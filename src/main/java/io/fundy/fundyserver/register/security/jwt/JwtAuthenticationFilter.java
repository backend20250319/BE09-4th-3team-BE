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

    /** skipPaths 에 매칭되는 요청은 이 필터를 적용하지 않음 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return skipPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Header 또는 Cookie에서 토큰 추출
        String token = resolveToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            String userId = tokenProvider.getUserId(token);

            // 2. 일반 회원 인증 시도 → 실패하면 OAuth 회원 인증 시도
            boolean authenticated = tryUserAuth(userId, token) || tryOAuthUserAuth(userId, token);

            if (authenticated) {
                log.info("[JWT 인증 성공] userId(email): {}", userId);
            } else {
                log.warn("[JWT 인증 실패] 토큰은 유효하지만 사용자 정보 없음: {}", userId);
            }
        }

        filterChain.doFilter(request, response);
    }

    /** JWT를 Header 또는 Cookie에서 추출 */
    private String resolveToken(HttpServletRequest request) {
        // 1-1. Authorization 헤더
        String header = request.getHeader("Authorization");
        if (header != null && header.toLowerCase().startsWith("bearer ")) {
            return header.substring(7);
        }

        // 1-2. accessToken 쿠키
        if (request.getCookies() != null) {
            Optional<Cookie> accessTokenCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .findFirst();

            if (accessTokenCookie.isPresent()) {
                return accessTokenCookie.get().getValue();
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

    /** OAuth 회원 인증 (BAN 상태 체크 포함) */
    private boolean tryOAuthUserAuth(String email, String token) {
        return oauthUserRepo.findByEmail(email)
                .map(user -> {
                    if (user.getRole() != null && user.getRole().name().equalsIgnoreCase("BAN")) {
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
