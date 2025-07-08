package io.fundy.fundyserver.register.security.jwt;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import io.fundy.fundyserver.register.security.jwt.JwtTokenProvider;
import io.fundy.fundyserver.register.repository.OAuthUserRepository;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepo;
    private final OAuthUserRepository oauthUserRepo;
    private final List<String> skipPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserRepository userRepo,
            OAuthUserRepository oauthUserRepo,
            List<String> skipPaths
    ) {
        this.tokenProvider = tokenProvider;
        this.userRepo = userRepo;
        this.oauthUserRepo = oauthUserRepo;
        this.skipPaths = skipPaths;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) 토큰 추출
        String token = resolveToken(request);
        log.debug("Resolved token: {}", token);

        // 2) 보호된 경로면 토큰 없거나 유효하지 않을 때 401 반환
        if (!shouldNotFilter(request)) {
            if (token == null) {
                log.warn("[JWT 인증 실패] 토큰 누락: {}", request.getServletPath());
                sendUnauthorized(response, "Authorization 토큰이 필요합니다.");
                return;
            }
            // 만료 or 기타 JWT 예외를 명확하게 구분해서 401 메시지 다르게 처리
            try {
                if (!tokenProvider.validateToken(token)) {
                    log.warn("[JWT 인증 실패] 토큰 유효성 검증 실패: {}", token);
                    sendUnauthorized(response, "유효하지 않은 토큰입니다.");
                    return;
                }
            } catch (ApiException ex) {
                if (ex.getErrorCode() == ErrorCode.TOKEN_EXPIRED) {
                    log.warn("[JWT 인증 실패] 토큰 만료: {}", token);
                    // ★ 순환참조 방지: 여기서 userService 사용하지 않음!
                    sendUnauthorized(response, "만료된 토큰입니다.");
                    return;
                } else {
                    log.warn("[JWT 인증 실패] 기타 JWT 에러: {}", ex.getMessage());
                    sendUnauthorized(response, "유효하지 않은 토큰입니다.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    protected boolean shouldNotFilter(HttpServletRequest request) {
        return skipPaths.stream().anyMatch(path -> pathMatcher.match(path, request.getServletPath()));
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
