package io.fundy.fundyserver.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundy.fundyserver.notification.config.RabbitMQConfig;
import io.fundy.fundyserver.notification.dto.NotificationMessageDTO;
import io.fundy.fundyserver.notification.dto.NotificationResponseDTO;
import io.fundy.fundyserver.notification.entity.Notification;
import io.fundy.fundyserver.notification.repository.NotificationRepository;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final NotificationRepository notificationRepository;


    /**
     * 후원 완료 알림 발송 메서드
     * 후원자와 프로젝트 창작자에게 각각 알림 메시지를 큐로 전송한다.
     *
     * @param userId        후원자 사용자 ID
     * @param projectNo     프로젝트 번호
     * @param projectTitle  프로젝트 제목
     * @param supporterName 후원자 닉네임
     */
    public void sendSupportComplete(String userId, Long projectNo, String projectTitle, String supporterName) {
        String supporterMessage = projectTitle + " 프로젝트에 후원이 완료되었습니다.";
        sendToQueue("후원 완료", supporterMessage, userId, projectNo);

        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        String creatorId = project.getUser().getUserId();

        if (!creatorId.equals(userId)) {
            String creatorMessage = supporterName + " 님이 " + projectTitle + " 프로젝트에 후원하였습니다.";
            sendToQueue("후원 완료", creatorMessage, creatorId, projectNo);
        }
    }


    /**
     * 프로젝트 성공 마감 알림 발송 메서드
     * 창작자와 후원자 전체에게 성공 알림을 전송한다.
     *
     * @param projectTitle  프로젝트 제목
     * @param projectNo     프로젝트 번호
     * @param creatorId     창작자 사용자 ID
     * @param supporterIds  후원자 사용자 ID 리스트
     */
    public void sendProjectSuccess(String projectTitle, Long projectNo, String creatorId, List<String> supporterIds) {
        String message = "등록한 " + projectTitle + " 프로젝트가 성공적으로 종료되었습니다!";

        // 창작자에게 알림
        sendToQueue("프로젝트 마감 (성공)", message, creatorId, projectNo);

        // 후원자 전체에게 알림 (창작자 중복 제외)
        for (String supporterId : supporterIds) {
            if (!supporterId.equals(creatorId)) {
                sendToQueue("프로젝트 마감 (성공)", message, supporterId, projectNo);
            }
        }
    }

    /**
     * 프로젝트 실패 마감 알림 발송 메서드
     * 창작자와 후원자 전체에게 실패 알림을 전송한다.
     *
     * @param projectTitle  프로젝트 제목
     * @param projectNo     프로젝트 번호
     * @param creatorId     창작자 사용자 ID
     * @param supporterIds  후원자 사용자 ID 리스트
     */
    public void sendProjectFail(String projectTitle, Long projectNo, String creatorId, List<String> supporterIds) {
        String message = "등록한 " + projectTitle + " 프로젝트가 목표 금액 미달로 종료되었습니다.";

        // 창작자에게 알림
        sendToQueue("프로젝트 마감 (실패)", message, creatorId, projectNo);

        // 후원자 전체에게 알림 (창작자 중복 제외)
        for (String supporterId : supporterIds) {
            if (!supporterId.equals(creatorId)) {
                sendToQueue("프로젝트 마감 (실패)", message, supporterId, projectNo);
            }
        }
    }

    /**
     * 알림 소프트 삭제 처리 메서드
     * 알림의 isDeleted 플래그를 true로 설정하여 삭제 처리(물리 삭제 아님)
     *
     * @param notificationNo 삭제할 알림 ID
     * @param userId         요청한 사용자 ID (본인 알림만 삭제 가능)
     * @throws AccessDeniedException 권한이 없을 경우 예외 발생
     */
    @Transactional
    public void deleteNotification(Long notificationNo, String userId) {
        Notification notification = notificationRepository.findById(notificationNo)
                .orElseThrow(() -> new RuntimeException("해당 알림이 존재하지 않습니다."));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("알림 삭제 권한이 없습니다.");
        }

        notification.markAsDeleted();
        notificationRepository.save(notification);
    }

    /**
     * 읽지 않은, 삭제되지 않은 알림 개수 조회
     *
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    /**
     * 모든 읽지 않은 알림을 읽음 처리
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void markAllNotificationsAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUser_UserIdAndIsReadFalseAndIsDeletedFalse(userId);
        unread.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unread);
    }

    /**
     * 사용자 ID와 알림 타입별 알림 목록 조회
     * 페이징 및 정렬(최신순) 적용
     *
     * @param userId 사용자 ID
     * @param type   알림 타입 (completed, success, fail, all 등)
     * @param page   페이지 번호 (0부터 시작)
     * @param size   페이지 크기
     * @return 페이징된 NotificationResponseDTO 목록
     */
    public Page<NotificationResponseDTO> getNotificationsByUserAndType(String userId, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage;
        if (type == null || "all".equalsIgnoreCase(type)) {
            notificationPage = notificationRepository.findByUser_UserIdAndIsDeletedFalse(userId, pageable);
        } else {
            String mappedType = switch (type) {
                case "completed" -> "후원 완료";
                case "success" -> "프로젝트 마감 (성공)";
                case "fail" -> "프로젝트 마감 (실패)";
                default -> throw new IllegalArgumentException("알 수 없는 알림 타입입니다: " + type);
            };
            notificationPage = notificationRepository.findByUser_UserIdAndTypeAndIsDeletedFalse(userId, mappedType, pageable);
        }

        return notificationPage.map(n -> new NotificationResponseDTO(
                n.getNotificationNo(),
                n.getProject().getProjectNo(),
                n.getProject().getTitle(),
                n.getType(),
                n.getMessage(),
                n.getIsRead(),
                n.getCreatedAt(),
                n.getProject().getCreatorName(),
                n.getProject().getThumbnailUrl()
        ));
    }

    /**
     * RabbitMQ 큐로 알림 메시지 전송
     *
     * @param type      알림 타입
     * @param content   알림 메시지 내용
     * @param userId    수신자 사용자 ID
     * @param projectNo 관련 프로젝트 번호
     */
    private void sendToQueue(String type, String content, String userId, Long projectNo) {
        NotificationMessageDTO dto = NotificationMessageDTO.builder()
                .type(type)
                .message(content)
                .userId(userId)
                .projectNo(projectNo)
                .build();

        try {
            String jsonMessage = objectMapper.writeValueAsString(dto);
            log.info("📬 큐에 메시지 전송됨 → type: {}, userId: {}, projectNo: {}, message: {}",
                    type, userId, projectNo, content);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    jsonMessage
            );
        } catch (Exception e) {
            log.error("❌ 알림 메시지 큐 전송 실패 (비즈니스 로직은 계속 진행)", e);
        }
    }

}