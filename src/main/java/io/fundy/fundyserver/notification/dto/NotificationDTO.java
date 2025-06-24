package io.fundy.fundyserver.notification.dto;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Long notificationId;
    private Long userId;
    private String nickname;  // User 닉네임 (조회 시 조인)
    private Long projectId;
    private String type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
