// ✅ RegisterController.java
package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.*;
import io.fundy.fundyserver.register.entity.RefreshToken;
import io.fundy.fundyserver.register.entity.UserStatus;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.repository.RefreshTokenRepository;
import io.fundy.fundyserver.register.security.jwt.JwtTokenProvider;
import io.fundy.fundyserver.register.service.AddressService;
import io.fundy.fundyserver.register.service.UserService;
import io.fundy.fundyserver.register.dto.UserUpdateRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenRepository refreshRepo;
    private final AddressService addressService;

    // ✅ 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody UserRequestDTO req) {
        UserResponseDTO res = userService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ✅ 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginReq) {
        log.info("✅ 로그인 요청 수신: userId={}, password=****", loginReq.getUserId());

        UserResponseDTO user = userService.login(loginReq.getUserId(), loginReq.getPassword());
        log.info("✅ 로그인 성공: userNo={}, nickname={}, role={}", user.getUserNo(), user.getNickname(), user.getRoleType());

        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getRoleType());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        log.info("✅ JWT 생성 완료");

        RefreshToken tokenEntity = refreshRepo.findById(user.getUserId())
                .orElse(RefreshToken.builder()
                        .userId(user.getUserId())
                        .token(refreshToken)
                        .expiryDate(Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiryMs()))
                        .build());

        refreshRepo.save(tokenEntity);
        log.info("✅ RefreshToken 저장 완료");

        long expiresInMs = jwtProvider.getProps().getAccessTokenExpireMs();
        TokenResponseDTO tokens = TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(expiresInMs)
                .build();

        log.info("✅ 로그인 처리 완료 → 토큰 응답 반환");
        return ResponseEntity.ok(tokens);
    }

    // ✅ 현재 로그인 유저 정보 (JWT 기반, **이것만 남김**)
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
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
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer id = userService.getUserByUserId(userId).getUserNo();

        UserResponseDTO updated = userService.updateUser(id, req);
        return ResponseEntity.ok(updated);
    }

    // ✅ 로그아웃
    @PostMapping("/user/me/logout")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            log.info("로그아웃 요청 userId: {}", userId);
            userService.logout(userId);
            log.info("로그아웃 성공 userId: {}", userId);
            return ResponseEntity.ok("로그아웃이 성공 하였습니다.");
        } catch (Exception e) {
            log.error("로그아웃 중 에러: {}", e.getMessage(), e);
            throw e; // 기존 핸들러에서 처리됨
        }
    }

    // ✅ 비밀번호 변경
    @PatchMapping("/user/me/password_update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PasswordChangeRequestDTO req
    ) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer id = userService.getUserByUserId(userId).getUserNo();

        userService.changePassword(id, req);
        return ResponseEntity.ok("비밀번호 변경 하였습니다.");
    }

    // ✅ 회원 탈퇴
    @DeleteMapping("/user/me_quit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer id = userService.getUserByUserId(userId).getUserNo();

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

        String userId = jwtProvider.getUserId(stored.getToken());
        String newAccess = jwtProvider.createAccessToken(userId, jwtProvider.getRole(stored.getToken()));
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

    // ✅ 프로필 정보 수정
    @PatchMapping("/user/me/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateRequestDTO req
    ) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer id = userService.getUserByUserId(userId).getUserNo();

        UserResponseDTO updated = userService.updateUserProfile(id, req);
        return ResponseEntity.ok(updated);
    }

    // ========== 배송지 관련 API ==========

    // ✅ 배송지 등록
    @PostMapping("/user/me/addresses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AddressResponseDTO> addAddress(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AddressRequestDTO req
    ) {
        log.info("✅ 배송지 등록 요청 수신: name={}, phone={}", req.getName(), req.getPhone());

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer userNo = userService.getUserByUserId(userId).getUserNo();

        AddressResponseDTO address = addressService.addAddress(userNo, req);
        log.info("✅ 배송지 등록 완료: addressId={}", address.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    // ✅ 배송지 목록 조회
    @GetMapping("/user/me/addresses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<AddressResponseDTO>> getAddresses(
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("✅ 배송지 목록 조회 요청 수신");

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer userNo = userService.getUserByUserId(userId).getUserNo();

        List<AddressResponseDTO> addresses = addressService.getAddressesByUserNo(userNo);
        log.info("✅ 배송지 목록 조회 완료: count={}", addresses.size());

        return ResponseEntity.ok(addresses);
    }

    // ✅ 배송지 삭제
    @DeleteMapping("/user/me/addresses/{addressId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId
    ) {
        log.info("✅ 배송지 삭제 요청 수신: addressId={}", addressId);

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer userNo = userService.getUserByUserId(userId).getUserNo();

        addressService.deleteAddress(userNo, addressId);
        log.info("✅ 배송지 삭제 완료: addressId={}", addressId);

        return ResponseEntity.ok("배송지가 삭제되었습니다.");
    }

    // ✅ 기본 배송지 설정
    @PatchMapping("/user/me/addresses/{addressId}/default")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> setDefaultAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId
    ) {
        log.info("✅ 기본 배송지 설정 요청 수신: addressId={}", addressId);

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer userNo = userService.getUserByUserId(userId).getUserNo();

        addressService.setDefaultAddress(userNo, addressId);
        log.info("✅ 기본 배송지 설정 완료: addressId={}", addressId);

        return ResponseEntity.ok("기본 배송지로 설정되었습니다.");
    }

    // ✅ 배송지 수정
    @PutMapping("/user/me/addresses/{addressId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequestDTO req
    ) {
        log.info("✅ 배송지 수정 요청 수신: addressId={}", addressId);

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        Integer userNo = userService.getUserByUserId(userId).getUserNo();

        AddressResponseDTO updated = addressService.updateAddress(userNo, addressId, req);
        log.info("✅ 배송지 수정 완료: addressId={}", addressId);

        return ResponseEntity.ok(updated);
    }
}