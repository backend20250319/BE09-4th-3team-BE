package io.fundy.fundyserver.register.security.jwt;

import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.entity.RoleType;
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
        // HS256에 적합한 키 생성 (256bit 이상)
        this.secretKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** Access Token 생성 */
    public String createAccessToken(String userId, RoleType role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getAccessTokenExpireMs());

        return Jwts.builder()
                .subject(userId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getRefreshTokenExpireMs());

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
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

    // 사용자 ID 추출
    public String getUserId(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    // 역할(RoleType) 추출
    public RoleType getRole(String token) {
        String role = parseClaims(token).getPayload().get("role", String.class);
        return RoleType.valueOf(role);
    }

    // Claims 파싱 및 예외 처리
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
    public long getRefreshTokenExpiryMs() {
        return props.getRefreshTokenExpireMs();
    }

    // 외부에서 props 값 참조
    public JwtProperties getProps() {
        return props;
    }
}
