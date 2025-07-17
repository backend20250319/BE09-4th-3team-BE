package io.fundy.fundyserver.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundy.fundyserver.notification.dto.NotificationMessageDTO;

public class ObjectMapperExample {
    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // JSON -> 자바 객체
        String json = "{\"userNo\":1,\"projectNo\":2,\"type\":\"후원완료\",\"message\":\"OOO 프로젝트에 후원이 완료되었습니다.\"}";
        NotificationMessageDTO dto = objectMapper.readValue(json, NotificationMessageDTO.class);

        // 자바 객체 -> JSON
        String jsonString = objectMapper.writeValueAsString(dto);
        System.out.println(jsonString);
    }
}