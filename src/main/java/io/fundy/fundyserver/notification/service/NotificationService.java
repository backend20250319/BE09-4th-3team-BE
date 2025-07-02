package io.fundy.fundyserver.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundy.fundyserver.notification.config.RabbitMQConfig;
import io.fundy.fundyserver.notification.dto.NotificationMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    // ① 후원 완료 알림
    public void sendSupportComplete(Integer userNo, Long projectNo, String projectTitle) {
        String message = projectTitle + " 프로젝트에 후원이 완료되었습니다.";
        sendToQueue("후원 완료", message, userNo, projectNo);
    }

    // ② 프로젝트 성공 마감 알림
    public void sendProjectSuccess(Integer userNo, Long projectNo, String projectTitle) {
        String message = projectTitle + " 프로젝트가 성공적으로 종료되었습니다!";
        sendToQueue("프로젝트 마감 (성공)", message, userNo, projectNo);
    }

    // ③ 프로젝트 실패 마감 알림
    public void sendProjectFail(Integer userNo, Long projectNo, String projectTitle) {
        String message = projectTitle + " 프로젝트가 목표 금액 미달로 종료되었습니다. 후원이 취소됩니다.";
        sendToQueue("프로젝트 마감 (실패)", message, userNo, projectNo);
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