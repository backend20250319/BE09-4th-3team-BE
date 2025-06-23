// UserResponse.java
package io.fundy.fundyserver.dto;

import io.fundy.fundyserver.entity.RoleType;
import io.fundy.fundyserver.entity.UserStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Integer id;
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
}
