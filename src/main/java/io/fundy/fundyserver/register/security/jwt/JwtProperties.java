package io.fundy.fundyserver.register.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    // Base64로 인코딩된 비밀키
    private String secret;
    // AccessToken 만료시간 (밀리초)
    private long accessTokenExpireMs;
    // RefreshToken 만료시간 (밀리초)
    private long refreshTokenExpireMs;
}