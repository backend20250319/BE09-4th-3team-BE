package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.*;
import io.fundy.fundyserver.register.entity.RefreshToken;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.entity.UserStatus;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.repository.RefreshTokenRepository;
import io.fundy.fundyserver.register.security.jwt.JwtTokenProvider;
import io.fundy.fundyserver.register.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenRepository refreshRepo;

    // ✅ 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody UserRequestDTO req) {
        UserResponseDTO res = userService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ✅ 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginReq) {
        UserResponseDTO user = userService.login(loginReq.getUserId(), loginReq.getPassword());

        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getRoleType());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        RefreshToken tokenEntity = refreshRepo.findById(user.getUserId())
                .map(rt -> rt.toBuilder()
                        .token(refreshToken)
                        .expiryDate(Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiryMs()))
                        .build())
                .orElse(RefreshToken.builder()
                        .userId(user.getUserId())
                        .token(refreshToken)
                        .expiryDate(Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiryMs()))
                        .build());
        refreshRepo.save(tokenEntity);

        long expiresInMs = jwtProvider.getProps().getAccessTokenExpireMs();
        TokenResponseDTO tokens = TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(expiresInMs)
                .build();

        return ResponseEntity.ok(tokens);
    }

    // ✅ 유저 조회 (ID로)
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // ✅ 현재 로그인 유저
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token  = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        UserResponseDTO user = userService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }

    // ✅ 유저 정보 수정
    @PatchMapping("/user/me/update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserRequestDTO req
    ) {
        String token  = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer id    = userService.getUserByUserId(userId).getUserNo();

        UserResponseDTO updated = userService.updateUser(id, req);
        return ResponseEntity.ok(updated);
    }

    // ✅ 로그아웃
    @PostMapping("/user/me/logout")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token  = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        userService.logout(userId);
        return ResponseEntity.ok("로그아웃이 성공 하였습니다.");
    }

    // ✅ 비밀번호 변경
    @PatchMapping("/user/me/password_update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PasswordChangeRequestDTO req
    ) {
        String token  = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer id    = userService.getUserByUserId(userId).getUserNo();

        userService.changePassword(id, req);
        return ResponseEntity.ok("비밀번호 변경 하였습니다.");
    }

    // ✅ 회원 탈퇴
    @DeleteMapping("/user/me_quit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        String token  = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer id    = userService.getUserByUserId(userId).getUserNo();

        userService.updateUserStatus(id, UserStatus.QUIT);
        return ResponseEntity.ok("회원 탈퇴 처리 되었습니다.");
    }

    // ✅ 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO req) {
        RefreshToken stored = refreshRepo.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        }

        String userId     = jwtProvider.getUserId(stored.getToken());
        String newAccess  = jwtProvider.createAccessToken(userId, jwtProvider.getRole(stored.getToken()));
        String newRefresh = jwtProvider.createRefreshToken(userId);

        stored.setToken(newRefresh);
        stored.setExpiryDate(Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiryMs()));
        refreshRepo.save(stored);

        TokenResponseDTO res = TokenResponseDTO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
        return ResponseEntity.ok(res);
    }

    // ✅ 아이디 중복 확인
    @GetMapping("/check-user-id")
    public ResponseEntity<Boolean> checkUserId(@RequestParam String userId) {
        return ResponseEntity.ok(userService.isUserIdDuplicate(userId));
    }

    // ✅ 닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.isNicknameDuplicate(nickname));
    }

    // ✅ 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.isEmailDuplicate(email));
    }

    // ✅ 전화번호 중복 확인
    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhone(@RequestParam String phone) {
        return ResponseEntity.ok(userService.isPhoneDuplicate(phone));
    }
}
