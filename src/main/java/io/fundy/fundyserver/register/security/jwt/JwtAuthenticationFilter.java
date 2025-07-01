package io.fundy.fundyserver.register.security.jwt;

import io.fundy.fundyserver.register.entity.UserStatus;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.OAuthUserRepository;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.register.security.CustomOAuthUserDetails;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j  // ← 1. 로거 추가 (기존 @Slf4j 유지)
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

    /** skipPaths 에 매칭되는 요청은 이 필터를 적용하지 않음 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean skip = skipPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        log.debug("shouldNotFilter? [{}] -> {}", path, skip);  // ← 2. 디버깅용 로그 추가
        return skip;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) 토큰 추출
        String token = resolveToken(request);
        log.debug("Resolved token: {}", token);  // ← 3. 추출된 토큰 로그

        // 2) 보호된 경로면 토큰 없거나 유효하지 않을 때 바로 401 응답
        if (!shouldNotFilter(request)) {
            if (token == null) {
                log.warn("[JWT 인증 실패] 토큰 누락: {}", request.getServletPath());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization 토큰이 필요합니다.");
                return;  // 체인 종료
            }
            if (!tokenProvider.validateToken(token)) {
                log.warn("[JWT 인증 실패] 토큰 유효성 검증 실패: {}", token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
                return;  // 체인 종료
            }

            // 3) 토큰이 유효하면 userId 추출 → 인증 컨텍스트 설정
            String userId = tokenProvider.getUserId(token);
            boolean authenticated = tryUserAuth(userId, token) || tryOAuthUserAuth(userId, token);
            if (authenticated) {
                log.info("[JWT 인증 성공] userId: {}", userId);
            } else {
                log.warn("[JWT 인증 실패] 유효한 토큰이나 사용자 없음: {}", userId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "사용자를 찾을 수 없습니다.");
                return;
            }
        }

        // 4) 다음 필터/컨트롤러로 진행
        filterChain.doFilter(request, response);
    }

    /** JWT를 Header 또는 Cookie에서 추출 */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.toLowerCase().startsWith("bearer ")) {
            return header.substring(7);
        }
        if (request.getCookies() != null) {
            Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .findFirst();
            if (cookie.isPresent()) {
                return cookie.get().getValue();
            }
        }
        return null;
    }

    /** 일반 회원 인증 */
    private boolean tryUserAuth(String userId, String token) {
        return userRepo.findByUserIdAndUserStatusNot(userId, UserStatus.BANNED)
                .map(user -> {
                    CustomUserDetails principal = new CustomUserDetails(user);
                    setAuthContext(principal, token);
                    return true;
                }).orElse(false);
    }

    /** OAuth 회원 인증 */
    private boolean tryOAuthUserAuth(String email, String token) {
        return oauthUserRepo.findByEmail(email)
                .map(user -> {
                    if (user.getRole() != null && "BAN".equalsIgnoreCase(user.getRole().name())) {
                        throw new ApiException(ErrorCode.BANNED_USER);
                    }
                    CustomOAuthUserDetails principal = new CustomOAuthUserDetails(user);
                    setAuthContext(principal, token);
                    return true;
                }).orElse(false);
    }

    /** 인증 컨텍스트 설정 */
    private void setAuthContext(Object principal, String token) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        token,
                        ((org.springframework.security.core.userdetails.UserDetails) principal).getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
