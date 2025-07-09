package io.fundy.fundyserver.register.security.jwt;

import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties props;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(String userId, RoleType role) {
        return buildToken(userId, props.getAccessTokenExpireMs(), role);
    }

    // Refresh Token 생성 (Role 없이 생성)
    public String createRefreshToken(String userId) {
        return buildToken(userId, props.getRefreshTokenExpireMs(), null);
    }

    // 공통 토큰 JWT 서명 및 만료 정보 생성 로직
    private String buildToken(String userId, long expirationMs, RoleType role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        JwtBuilder builder = Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256);

        if (role != null) {
            builder.claim("role", role.name());
        }

        return builder.compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    //사용자 ID(email) 추출
    public String getUserId(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    // 역할(UserRole) 추출
    public RoleType getRole(String token) {
        String role = parseClaims(token).getPayload().get("role", String.class);
        return RoleType.valueOf(role);
    }

    // Claims 파싱 (예외 핸들링 포함)
    private Jws<Claims> parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
    }

    // RefreshToken 만료 시간 반환
    public long getRefreshTokenExpiryMs() {
        return props.getRefreshTokenExpireMs();
    }

    // 설정 반환
    public JwtProperties getProps() {
        return props;
    }

    //  토큰 만료시간(exp) 반환 - 만료시각(밀리초)
    public Date getTokenExpiry(String token) {
        return parseClaims(token).getPayload().getExpiration();
    }

    // 토큰이 만료됐는지 즉시 체크 (true = 만료)
    public boolean isTokenExpired(String token) {
        try {
            Date exp = getTokenExpiry(token);
            return exp.before(new Date());
        } catch (ApiException e) {
            return true;
        }
    }
}
