package io.fundy.fundyserver.notification.dto;

import lombok.*;

@Getter
@Builder
public class NotificationMessageDTO {
    private Integer userNo;
    private Long projectNo;
    private String type;
    private String message;
}