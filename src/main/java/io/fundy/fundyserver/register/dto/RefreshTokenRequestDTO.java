package io.fundy.fundyserver.register.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

// 리프레쉬 토큰 관련 DTO
public class RefreshTokenRequestDTO {
    @NotBlank(message = "리프레시 토큰을 입력하세요.")
    private String refreshToken;
}