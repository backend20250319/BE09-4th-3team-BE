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
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    // 알림 목록 조회
    public List<NotificationResponseDTO> getNotificationsByUserAndType(Integer userNo, String type) {
        List<Notification> notifications;

        if ("all".equalsIgnoreCase(type)) {
            notifications = notificationRepository.findByUser_UserNo(userNo);
        } else {
            notifications = notificationRepository.findByUser_UserNoAndType(userNo, type);
        }

        return notifications.stream()
                .map(n -> new NotificationResponseDTO(
                        n.getNotificationNo(),
                        n.getProject().getProjectNo(),
                        n.getProject().getTitle(),
                        n.getType(),
                        n.getMessage(),
                        n.getIsRead(),
                        n.getCreatedAt(),
                        n.getUser().getNickname()
                ))
                .collect(Collectors.toList());
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