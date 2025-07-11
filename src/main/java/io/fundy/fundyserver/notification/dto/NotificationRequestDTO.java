package io.fundy.fundyserver.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private String userId;
    private String type = "all";
    private int page = 0;
    private int size = 5;
}