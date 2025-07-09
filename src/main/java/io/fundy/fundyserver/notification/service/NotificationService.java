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
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {


    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final ParticipationRepository participationRepository;
    private final NotificationRepository notificationRepository;

    // 프로젝트 존재 확인 및 유저 참여 여부 검증
    private Project validateProjectAndParticipation(String userId, Long projectNo) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        if (!participationRepository.existsByUser_UserIdAndProject_ProjectNo(userId, projectNo)) {
            throw new IllegalArgumentException("해당 유저는 이 프로젝트에 참여하지 않았습니다.");
        }
        return project;
    }

    // 후원 완료 알림 발송
    public void sendSupportComplete(String userId, Long projectNo, String projectTitle) {
        validateProjectAndParticipation(userId, projectNo);
        String message = projectTitle + " 프로젝트에 후원이 완료되었습니다.";
        sendToQueue("후원 완료", message, userId, projectNo);
    }

    // 프로젝트 성공 마감 알림 발송
    public void sendProjectSuccess(String userId, Long projectNo, String projectTitle) {
        Project project = validateProjectAndParticipation(userId, projectNo);

        LocalDate today = LocalDate.now();
        if (!today.isAfter(project.getDeadLine())) {
            throw new IllegalStateException("프로젝트 마감일이 지나지 않았습니다.");
        }
        if (project.getCurrentAmount() < project.getGoalAmount()) {
            throw new IllegalStateException("목표 금액이 채워지지 않았습니다.");
        }

        String message = projectTitle + " 프로젝트가 성공적으로 종료되었습니다!";
        sendToQueue("프로젝트 마감 (성공)", message, userId, projectNo);
    }

    // 프로젝트 실패 마감 알림 발송
    public void sendProjectFail(String userId, Long projectNo, String projectTitle) {
        Project project = validateProjectAndParticipation(userId, projectNo);

        LocalDate today = LocalDate.now();
        if (!today.isAfter(project.getDeadLine())) {
            throw new IllegalStateException("프로젝트 마감일이 지나지 않았습니다.");
        }
        if (project.getCurrentAmount() >= project.getGoalAmount()) {
            throw new IllegalStateException("프로젝트가 성공적으로 마감되었습니다.");
        }

        String message = projectTitle + " 프로젝트가 목표 금액 미달로 종료되었습니다. 후원이 취소됩니다.";
        sendToQueue("프로젝트 마감 (실패)", message, userId, projectNo);
    }

    // 알림 소프트 삭제 처리 (isDeleted = true)
    @Transactional
    public void deleteNotification(Long notificationNo, String userId) {
        Notification notification = notificationRepository.findById(notificationNo)
                .orElseThrow(() -> new RuntimeException("해당 알림이 존재하지 않습니다."));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("알림 삭제 권한이 없습니다.");
        }

        notification.markAsDeleted(); // 삭제 상태 표시
        notificationRepository.save(notification); // 변경 저장
    }

    // 읽지 않은 삭제되지 않은 알림 개수 조회
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    // 모든 읽지 않은 알림을 읽음 처리
    @Transactional
    public void markAllNotificationsAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUser_UserIdAndIsReadFalseAndIsDeletedFalse(userId);
        unread.forEach(Notification::markAsRead); // 읽음 상태 변경
        notificationRepository.saveAll(unread); // 변경 저장
    }

    // 유저와 타입별 알림 목록 조회 (삭제되지 않은 것만)
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

        // DTO로 변환하여 반환
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