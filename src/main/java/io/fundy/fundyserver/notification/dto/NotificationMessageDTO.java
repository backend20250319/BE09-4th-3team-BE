package io.fundy.fundyserver.notification.dto;

import lombok.*;

@Getter
@Builder
public class NotificationMessageDTO {
    private String userId;
    private Long projectNo;
    private String type;
    private String message;
}