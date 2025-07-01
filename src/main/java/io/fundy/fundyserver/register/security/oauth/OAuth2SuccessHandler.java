package io.fundy.fundyserver.register.security.oauth;

import io.fundy.fundyserver.register.dto.oauth.SessionUser;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.security.jwt.JwtTokenProvider;
import io.fundy.fundyserver.register.service.OAuthUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthUserService oAuthUserService;
    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        log.debug("===== OAuth2SuccessHandler 진입 =====");

        // 1) Provider 구분자(google | kakao)
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        // 2) OAuth 프로필 정보
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String userNameAttrKey = authentication.getName(); // 필요시 사용

        // 3) DB 저장 및 업데이트
        OAuthUser savedUser = oAuthUserService.saveOrUpdate(registrationId, userNameAttrKey, attributes);
        savedUser.setRegistrationId(registrationId);
        log.debug("[OAuth2 Attributes] provider={}, attributes={}", registrationId, attributes);
        log.info("[OAuth2 로그인 성공] email={}", savedUser.getEmail());

        // 4) 세션 저장
        SessionUser sessionUser = new SessionUser(savedUser);
        httpSession.setAttribute("user", sessionUser);
        log.debug("SessionUser 저장 완료: {}", sessionUser);

        // 5) JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(savedUser.getEmail(), savedUser.getRoleType());
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getEmail());

        // 6) 쿠키로 전달
        Cookie atCookie = new Cookie("accessToken", accessToken);
        atCookie.setHttpOnly(true);
        atCookie.setPath("/");
        atCookie.setMaxAge((int) (jwtTokenProvider.getProps().getAccessTokenExpireMs() / 1000));
        response.addCookie(atCookie);

        Cookie rtCookie = new Cookie("refreshToken", refreshToken);
        rtCookie.setHttpOnly(true);
        rtCookie.setPath("/");
        rtCookie.setMaxAge((int) (jwtTokenProvider.getProps().getRefreshTokenExpireMs() / 1000));
        response.addCookie(rtCookie);

        // 7) 클라이언트로 리다이렉트 (향후 yml or .env로 추출 권장)
        String redirectUrl = "http://localhost:3000/seokgeun/home";  // FIXME: 추후 환경변수로 분리 권장
        log.debug("리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
