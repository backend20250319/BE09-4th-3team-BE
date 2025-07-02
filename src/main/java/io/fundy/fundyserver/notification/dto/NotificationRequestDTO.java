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
    private Long projectId;
    private Integer userNo;
    private Integer page = 0;
    private Integer size = 10;
}