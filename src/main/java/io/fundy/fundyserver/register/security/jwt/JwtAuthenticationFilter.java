package io.fundy.fundyserver.register.security.jwt;

import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.security.CustomOAuthUserDetails;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
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
            try {
                if (!tokenProvider.validateToken(token)) {
                    log.warn("[JWT 인증 실패] 토큰 유효성 검증 실패: {}", token);
                    sendUnauthorized(response, "유효하지 않은 토큰입니다.");
                    return;
                }
                // 1. 토큰에서 정보 추출
                String userId = tokenProvider.getUserId(token);
                RoleType roleType = tokenProvider.getRole(token);

                // 2. DB에서 일반 회원(User) 우선 조회
                Optional<User> userOpt = userRepo.findByEmail(userId);
                if (userOpt.isPresent()) {
                    CustomUserDetails userDetails = new CustomUserDetails(userOpt.get());
                    PreAuthenticatedAuthenticationToken authentication =
                            new PreAuthenticatedAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 3. 소셜 회원(OAuthUser) 조회
                    Optional<OAuthUser> oauthUserOpt = oauthUserRepo.findByEmail(userId);
                    if (oauthUserOpt.isPresent()) {
                        CustomOAuthUserDetails oauthUserDetails = new CustomOAuthUserDetails(oauthUserOpt.get());
                        PreAuthenticatedAuthenticationToken authentication =
                                new PreAuthenticatedAuthenticationToken(
                                        oauthUserDetails,
                                        null,
                                        oauthUserDetails.getAuthorities()
                                );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.warn("[JWT 인증 실패] 유저를 찾을 수 없음: {}", userId);
                        sendUnauthorized(response, "유저를 찾을 수 없습니다.");
                        return;
                    }
                }

            } catch (ApiException ex) {
                if (ex.getErrorCode() == ErrorCode.TOKEN_EXPIRED) {
                    log.warn("[JWT 인증 실패] 토큰 만료: {}", token);
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
