package io.fundy.fundyserver.register.dto;

import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.UserStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

// 회원가입 관련 DTO
public class UserResponseDTO {
    private Integer userNo;
    private String userId;
    private String nickname;
    private String email;
    private String phone;
    private String address;
    private String accountNumber;
    private UserStatus userStatus;
    private RoleType roleType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastLogoutAt;
}
