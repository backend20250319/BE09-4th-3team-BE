package io.fundy.fundyserver.register.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

// 토큰 관련 DTO
public class TokenResponseDTO {
    private String accessToken;           // 실제 API 호출에 사용하는 JWT
    private String refreshToken;          // 만료 시 새로운 Access Token을 받을 때 사용
    private long accessTokenExpiresIn;    // accessToken 유효 기간 (ms)
}
