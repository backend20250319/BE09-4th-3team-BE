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
    public ResponseEntity<SessionUser> me(@AuthenticationPrincipal Object principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        SessionUser sessionUser;
        if (principal instanceof CustomOAuthUserDetails customOAuthUserDetails) {
            OAuthUser oauthUser = customOAuthUserDetails.getOAuthUser();
            sessionUser = new SessionUser(oauthUser);
        } else if (principal instanceof CustomUserDetails customUserDetails) {
            User user = customUserDetails.getUser();
            sessionUser = new SessionUser(user);
        } else {
            log.error("알 수 없는 인증 주체: {}", principal.getClass());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
        }

        log.info("현재 로그인 유저 정보: name={}, email={}", sessionUser.getName(), sessionUser.getEmail());
        return ResponseEntity.ok(sessionUser);
    }

    // 프로필 수정 (닉네임, 이메일, 전화번호, 주소 등)
    @PatchMapping("/me/profile")
    public ResponseEntity<SessionUser> updateProfile(
            @AuthenticationPrincipal Object principal,
            @RequestBody ProfileUpdateRequest request) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            SessionUser updatedUser;
            if (principal instanceof CustomOAuthUserDetails customOAuthUserDetails) {
                OAuthUser oauthUser = customOAuthUserDetails.getOAuthUser();
                // OAuth 사용자 프로필 업데이트
                log.info("OAuth 사용자 프로필 업데이트 요청: nickname={}, email={}, phone={}, address={}",
                        request.getNickname(), request.getEmail(), request.getPhone(), request.getAddress());

                if (request.getNickname() != null) {
                    oauthUser.setNickname(request.getNickname());
                }
                if (request.getEmail() != null) {
                    oauthUser.setEmail(request.getEmail());
                }
                if (request.getPhone() != null) {
                    oauthUser.setPhone(request.getPhone());
                }
                if (request.getAddress() != null) {
                    oauthUser.setAddress(request.getAddress());
                }
                if (request.getAddressDetail() != null) {
                    oauthUser.setAddressDetail(request.getAddressDetail());
                }
                // 여기서 oauthUserRepository.save(oauthUser) 호출 필요
                updatedUser = new SessionUser(oauthUser);

            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                // 일반 사용자 프로필 업데이트
                log.info("일반 사용자 프로필 업데이트 요청: nickname={}, email={}, phone={}, address={}",
                        request.getNickname(), request.getEmail(), request.getPhone(), request.getAddress());

                if (request.getNickname() != null) {
                    user.setNickname(request.getNickname());
                }
                if (request.getEmail() != null) {
                    user.setEmail(request.getEmail());
                }
                if (request.getPhone() != null) {
                    user.setPhone(request.getPhone());
                }
                if (request.getAddress() != null) {
                    user.setAddress(request.getAddress());
                }
                if (request.getAddressDetail() != null) {
                    user.setAddressDetail(request.getAddressDetail());
                }
                // 여기서 userRepository.save(user) 호출 필요
                updatedUser = new SessionUser(user);
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            log.info("프로필 업데이트 성공: nickname={}, email={}",
                    updatedUser.getName(), updatedUser.getEmail());
            return ResponseEntity.ok(updatedUser);

        } catch (ResponseStatusException e) {
            throw e; // 이미 ResponseStatusException이면 그대로 던지기
        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 업데이트에 실패했습니다.");
        }
    }

    // 로그아웃 처리 (세션 무효화 + 쿠키 제거)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("로그아웃 요청 수신");

        // 1. 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("세션 무효화 완료");
        }

        // 2. JWT 쿠키 삭제
        deleteCookie("accessToken", response);
        deleteCookie("refreshToken", response);

        log.info("JWT 쿠키 삭제 완료");

        return ResponseEntity.noContent().build(); // 204 No Content
    }

    private void deleteCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setAttribute("SameSite", "None"); // 크로스 도메인 대응
        response.addCookie(cookie);
    }

    // 프로필 업데이트 요청 DTO
    public static class ProfileUpdateRequest {
        private String nickname;
        private String email;
        private String phone;
        private String address;
        private String addressDetail;

        // Getters and Setters
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getAddressDetail() { return addressDetail; }
        public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
    }
}