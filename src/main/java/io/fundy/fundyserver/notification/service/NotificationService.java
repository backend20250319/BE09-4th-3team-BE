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
import io.fundy.fundyserver.review.repository.ParticipationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.time.LocalDate;
import java.util.List;


@CrossOrigin(origins = "http://localhost:3000")
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final ParticipationRepository participationRepository;
    private final NotificationRepository notificationRepository;

    // ① 후원 완료 알림
    public void sendSupportComplete(Integer userNo, Long projectNo, String projectTitle) {

        projectRepository.findById(projectNo)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 유저-프로젝트 참여 검증 (후원했는지 확인)
        boolean participated = participationRepository.existsByUser_UserNoAndProject_ProjectNo(userNo, projectNo);
        if (!participated) {
            throw new IllegalArgumentException("해당 유저는 이 프로젝트에 참여하지 않았습니다.");
        }

        String message = projectTitle + " 프로젝트에 후원이 완료되었습니다.";
        sendToQueue("후원 완료", message, userNo, projectNo);
    }

    // 프로젝트 성공 마감 알림
    public void sendProjectSuccess(Integer userNo, Long projectNo, String projectTitle) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 유저-프로젝트 참여 검증
        boolean participated = participationRepository.existsByUser_UserNoAndProject_ProjectNo(userNo, projectNo);
        if (!participated) {
            throw new IllegalArgumentException("해당 유저는 이 프로젝트에 참여하지 않았습니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate deadline = project.getDeadLine();

        if (!today.isAfter(deadline)) {
            throw new IllegalStateException("프로젝트 마감일이 지나지 않았습니다.");
        }

        if (project.getCurrentAmount() < project.getGoalAmount()) {
            throw new IllegalStateException("목표 금액이 채워지지 않았습니다.");
        }

        String message = projectTitle + " 프로젝트가 성공적으로 종료되었습니다!";
        sendToQueue("프로젝트 마감 (성공)", message, userNo, projectNo);
    }

    // 프로젝트 실패 마감 알림
    public void sendProjectFail(Integer userNo, Long projectNo, String projectTitle) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 유저-프로젝트 참여 검증
        boolean participated = participationRepository.existsByUser_UserNoAndProject_ProjectNo(userNo, projectNo);
        if (!participated) {
            throw new IllegalArgumentException("해당 유저는 이 프로젝트에 참여하지 않았습니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate deadline = project.getDeadLine();

        if (!today.isAfter(deadline)) {
            throw new IllegalStateException("프로젝트 마감일이 지나지 않았습니다.");
        }

        if (project.getCurrentAmount() >= project.getGoalAmount()) {
            throw new IllegalStateException("프로젝트가 성공적으로 마감되었습니다.");
        }

        String message = projectTitle + " 프로젝트가 목표 금액 미달로 종료되었습니다. 후원이 취소됩니다.";
        sendToQueue("프로젝트 마감 (실패)", message, userNo, projectNo);
    }

    @Transactional
    public void deleteNotification(Long notificationNo, Integer userNo) {
        Notification notification = notificationRepository.findById(notificationNo)
                .orElseThrow(() -> new RuntimeException("해당 알림이 존재하지 않습니다."));

        // 보안 체크: 자기 알림만 삭제 가능
        if (!notification.getUser().getUserNo().equals(userNo)) {
            throw new AccessDeniedException("알림 삭제 권한이 없습니다.");
        }

        notificationRepository.delete(notification);
    }

    public long countUnreadNotifications(Integer userNo) {
        return notificationRepository.countByUser_UserNoAndIsReadFalse(userNo);
    }

    /**
     * 특정 사용자의 모든 읽지 않은 알림을 읽음 처리
     * @param userNo 사용자 번호
     */
    @Transactional
    public void markAllNotificationsAsRead(Integer userNo) {
        List<Notification> unreadNotifications = notificationRepository.findByUser_UserNoAndIsReadFalse(userNo);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    // 알림 목록 조회
    public Page<NotificationResponseDTO> getNotificationsByUserAndType(Integer userNo, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage;

        if (type == null || "all".equalsIgnoreCase(type)) {
            notificationPage = notificationRepository.findByUser_UserNo(userNo, pageable);
        } else {
            // 프론트에서 받은 영문 타입을 DB 저장된 한글 타입으로 매핑
            String mappedType = switch (type) {
                case "completed" -> "후원 완료";
                case "success" -> "프로젝트 마감 (성공)";
                case "fail" -> "프로젝트 마감 (실패)";
                default -> null;
            };

            if (mappedType == null) {
                throw new IllegalArgumentException("알 수 없는 알림 타입입니다: " + type);
            }

            notificationPage = notificationRepository.findByUser_UserNoAndType(userNo, mappedType, pageable);
        }

        return notificationPage.map(n -> new NotificationResponseDTO(
                n.getNotificationNo(),
                n.getProject().getProjectNo(),
                n.getProject().getTitle(),
                n.getType(),
                n.getMessage(),
                n.getIsRead(),
                n.getCreatedAt(),
                n.getUser().getNickname()
        ));
    }

    // 내부 공통 메서드
    private void sendToQueue(String type, String content, Integer userNo, Long projectNo) {
        NotificationMessageDTO dto = NotificationMessageDTO.builder()
                .type(type)
                .message(content)
                .userNo(userNo)
                .projectNo(projectNo)
                .build();

        try {
            String jsonMessage = objectMapper.writeValueAsString(dto);
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