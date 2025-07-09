package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.oauth.SessionUser;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.security.CustomOAuthUserDetails;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class OAuthUserController {


     // 로그인된 사용자 정보 조회
    @GetMapping("/me")
    public SessionUser me(@AuthenticationPrincipal Object principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        SessionUser sessionUser;
        if (principal instanceof CustomOAuthUserDetails) {
            OAuthUser oauthUser = ((CustomOAuthUserDetails) principal).getOAuthUser();
            sessionUser = new SessionUser(oauthUser);
        } else if (principal instanceof CustomUserDetails) {
            User user = ((CustomUserDetails) principal).getUser();
            sessionUser = new SessionUser(user);
        } else {
            throw new IllegalStateException("알 수 없는 인증 주체입니다: " + principal.getClass());
        }

        log.info("현재 로그인 유저 정보: name={}, email={}",
                sessionUser.getName(), sessionUser.getEmail());
        return sessionUser;
    }


     // 로그아웃 처리 (세션 무효화 + 쿠키 제거)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("🔒 로그아웃 요청 수신");

        // 1. 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("✅ 세션 무효화 완료");
        }

        // 2. JWT 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setHttpOnly(true);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);

        log.info("✅ JWT 쿠키 삭제 완료");

        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
