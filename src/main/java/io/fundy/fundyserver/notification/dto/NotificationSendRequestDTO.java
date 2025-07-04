package io.fundy.fundyserver.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendRequestDTO {
    private Integer userNo;
    private Long projectNo;
    private String projectTitle;
}