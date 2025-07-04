package io.fundy.fundyserver.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundy.fundyserver.notification.dto.NotificationMessageDTO;
import io.fundy.fundyserver.notification.entity.Notification;
import io.fundy.fundyserver.notification.repository.NotificationRepository;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification.queue")
    public void receiveNotification(String messageJson) {
        try {
            NotificationMessageDTO message = objectMapper.readValue(messageJson, NotificationMessageDTO.class);

            User user = userRepository.findById(message.getUserNo())
                    .orElseThrow(() -> new RuntimeException("유저 없음"));

            Project project = projectRepository.findById(message.getProjectNo())
                    .orElseThrow(() -> new RuntimeException("프로젝트 없음"));

            Notification notification = Notification.builder()
                    .user(user)
                    .project(project)
                    .type(message.getType())
                    .message(message.getMessage())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("알림 수신 처리 중 오류 발생", e);
        }
    }
}
