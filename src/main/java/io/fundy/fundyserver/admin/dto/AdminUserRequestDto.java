package io.fundy.fundyserver.admin.dto;

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