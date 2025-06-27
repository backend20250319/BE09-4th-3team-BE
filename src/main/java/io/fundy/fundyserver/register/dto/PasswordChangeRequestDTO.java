package io.fundy.fundyserver.register.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
// 패스워드 관련 DTO
public class PasswordChangeRequestDTO {

    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$",
            message = "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    private String newPassword;
}
