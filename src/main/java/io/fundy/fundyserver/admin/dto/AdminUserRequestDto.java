package io.fundy.fundyserver.admin.dto;

import io.fundy.fundyserver.register.entity.UserStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserRequestDto {

    private Integer userNo;             // 사용자 고유번호 (PK)
    private UserStatus userStatus;      // Enum: LOGIN, LOGOUT, BANNED
}
