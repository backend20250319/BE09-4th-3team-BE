package io.fundy.fundyserver.notification.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserRequestDto {
    private Integer userId;
    private String userStatus;
}