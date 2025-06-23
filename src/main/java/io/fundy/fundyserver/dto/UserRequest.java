// UserRequest.java
package io.fundy.fundyserver.dto;

import io.fundy.fundyserver.entity.RoleType;
import io.fundy.fundyserver.entity.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank @Size(max = 50)
    private String userId;

    @NotBlank @Size(min = 8, max = 255)
    private String password;

    @NotBlank @Size(max = 50)
    private String nickname;

    @NotBlank @Email @Size(max = 100)
    private String email;

    @NotBlank @Size(max = 20)
    private String phone;

    @Size(max = 200)
    private String address;

    @Size(max = 30)
    private String accountNumber;

    @NotNull
    private UserStatus userStatus;

    @NotNull
    private RoleType roleType;
}
