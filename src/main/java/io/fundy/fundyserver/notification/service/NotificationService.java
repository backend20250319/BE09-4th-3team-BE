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
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final NotificationRepository notificationRepository;


    // 후원 완료 알림 발송
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

    // 프로젝트 성공 마감 알림 발송
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
    // 프로젝트 실패 마감 알림 발송
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

    // 알림 소프트 삭제 처리 (isDeleted = true)
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

    // 읽지 않은 삭제되지 않은 알림 개수 조회
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    // 모든 읽지 않은 알림을 읽음 처리
    @Transactional
    public void markAllNotificationsAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUser_UserIdAndIsReadFalseAndIsDeletedFalse(userId);
        unread.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unread);
    }

    // 유저와 타입별 알림 목록 조회
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

    // RabbitMQ 큐로 알림 메시지 전송
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 직렬화 실패", e);
        }
    }
}