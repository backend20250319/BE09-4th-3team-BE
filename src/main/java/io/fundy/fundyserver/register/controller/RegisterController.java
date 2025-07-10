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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenRepository refreshRepo;
    private final AddressService addressService;

    //  회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody UserRequestDTO req) {
        UserResponseDTO res = userService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    //  로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginReq) {
        log.info(" 로그인 요청 수신: userId={}, password=****", loginReq.getUserId());

        // 1) 실제 유저 인증/조회
        UserResponseDTO user = userService.login(loginReq.getUserId(), loginReq.getPassword());
        log.info(" 로그인 성공: userNo={}, nickname={}, role={}", user.getUserNo(), user.getNickname(), user.getRoleType());

        // 2) JWT 실제 토큰 생성 (Access/Refresh)
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getRoleType());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        log.info(" JWT 생성 완료");

        // 3) RefreshToken DB 저장
        RefreshToken tokenEntity = refreshRepo.findById(user.getUserId())
                .orElse(RefreshToken.builder()
                        .userId(user.getUserId())
                        .token(refreshToken)
                        .expiryDate(Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiryMs()))
                        .build());

        refreshRepo.save(tokenEntity);
        log.info(" RefreshToken 저장 완료");

        // 4) 토큰 정보를 DTO로 포장
        long expiresInMs = jwtProvider.getProps().getAccessTokenExpireMs();
        TokenResponseDTO tokens = TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(expiresInMs)
                .build();

        log.info(" 로그인 처리 완료 → 토큰 응답 반환");
        return ResponseEntity.ok(tokens); // 이 부분에서 프론트로 토큰이 JSON 응답에 담겨 나감
    }

    //  현재 로그인 유저 정보 (JWT 기반)
    @GetMapping("/user/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            UserResponseDTO user = userService.getUserByUserId(userId);
            log.info(" 사용자 정보 조회 성공: userId={}, nickname={}", userId, user.getNickname());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error(" 사용자 정보 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    //  유저 정보 수정
    @PatchMapping("/user/me/update")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserRequestDTO req
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer id = userService.getUserByUserId(userId).getUserNo();

            UserResponseDTO updated = userService.updateUser(id, req);
            log.info(" 사용자 정보 수정 성공: userId={}", userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error(" 사용자 정보 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    //  로그아웃
    @PostMapping("/user/me/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            log.info(" 로그아웃 요청 userId: {}", userId);
            userService.logout(userId);
            log.info(" 로그아웃 성공 userId: {}", userId);
            return ResponseEntity.ok("로그아웃이 성공 하였습니다.");
        } catch (Exception e) {
            log.error(" 로그아웃 중 에러: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("로그아웃 처리 중 오류가 발생했습니다.");
        }
    }

    //  비밀번호 변경
    @PatchMapping("/user/me/password_update")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PasswordChangeRequestDTO req
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer id = userService.getUserByUserId(userId).getUserNo();

            userService.changePassword(id, req);
            log.info(" 비밀번호 변경 성공: userId={}", userId);
            return ResponseEntity.ok("비밀번호 변경 하였습니다.");
        } catch (Exception e) {
            log.error(" 비밀번호 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("비밀번호 변경 중 오류가 발생했습니다.");
        }
    }

    //  회원 탈퇴
    @DeleteMapping("/user/me_quit")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer id = userService.getUserByUserId(userId).getUserNo();

            userService.updateUserStatus(id, UserStatus.QUIT);
            log.info(" 회원 탈퇴 성공: userId={}", userId);
            return ResponseEntity.ok("회원 탈퇴 처리 되었습니다.");
        } catch (Exception e) {
            log.error(" 회원 탈퇴 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    //  토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO req) {
        try {
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
            log.info(" 토큰 재발급 성공: userId={}", userId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error(" 토큰 재발급 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    //  아이디 중복 확인
    @GetMapping("/check-user-id")
    public ResponseEntity<Boolean> checkUserId(@RequestParam String userId) {
        return ResponseEntity.ok(userService.isUserIdDuplicate(userId));
    }

    //  닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.isNicknameDuplicate(nickname));
    }

    //  이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.isEmailDuplicate(email));
    }

    //  전화번호 중복 확인
    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhone(@RequestParam String phone) {
        return ResponseEntity.ok(userService.isPhoneDuplicate(phone));
    }

    //  프로필 정보 수정
    @PatchMapping("/user/me/profile")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateRequestDTO req
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer id = userService.getUserByUserId(userId).getUserNo();

            UserResponseDTO updated = userService.updateUserProfile(id, req);
            log.info(" 프로필 정보 수정 성공: userId={}", userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error(" 프로필 정보 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    //  배송지 등록
    @PostMapping("/user/me/addresses")
    public ResponseEntity<AddressResponseDTO> addAddress(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AddressRequestDTO req
    ) {
        try {
            log.info(" 배송지 등록 요청 수신: name={}, phone={}", req.getName(), req.getPhone());

            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer userNo = userService.getUserByUserId(userId).getUserNo();

            AddressResponseDTO address = addressService.addAddress(userNo, req);
            log.info(" 배송지 등록 완료: addressId={}", address.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(address);
        } catch (Exception e) {
            log.error(" 배송지 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    //  배송지 목록 조회
    @GetMapping("/user/me/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getAddresses(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            log.info(" 배송지 목록 조회 요청 수신");

            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer userNo = userService.getUserByUserId(userId).getUserNo();

            List<AddressResponseDTO> addresses = addressService.getAddressesByUserNo(userNo);
            log.info(" 배송지 목록 조회 완료: count={}", addresses.size());

            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error(" 배송지 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    //  배송지 삭제
    @DeleteMapping("/user/me/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId
    ) {
        try {
            log.info(" 배송지 삭제 요청 수신: addressId={}", addressId);

            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer userNo = userService.getUserByUserId(userId).getUserNo();

            addressService.deleteAddress(userNo, addressId);
            log.info(" 배송지 삭제 완료: addressId={}", addressId);

            return ResponseEntity.ok("배송지가 삭제되었습니다.");
        } catch (Exception e) {
            log.error(" 배송지 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("배송지 삭제 중 오류가 발생했습니다.");
        }
    }

    //  기본 배송지 설정
    @PatchMapping("/user/me/addresses/{addressId}/default")
    public ResponseEntity<String> setDefaultAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId
    ) {
        try {
            log.info(" 기본 배송지 설정 요청 수신: addressId={}", addressId);

            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer userNo = userService.getUserByUserId(userId).getUserNo();

            addressService.setDefaultAddress(userNo, addressId);
            log.info(" 기본 배송지 설정 완료: addressId={}", addressId);

            return ResponseEntity.ok("기본 배송지로 설정되었습니다.");
        } catch (Exception e) {
            log.error(" 기본 배송지 설정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("기본 배송지 설정 중 오류가 발생했습니다.");
        }
    }

    //  배송지 수정
    @PutMapping("/user/me/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequestDTO req
    ) {
        try {
            log.info(" 배송지 수정 요청 수신: addressId={}", addressId);

            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer userNo = userService.getUserByUserId(userId).getUserNo();

            AddressResponseDTO updated = addressService.updateAddress(userNo, addressId, req);
            log.info(" 배송지 수정 완료: addressId={}", addressId);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error(" 배송지 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/user/me/profile-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            Integer userNo = userService.getUserByUserId(userId).getUserNo();

            // 이미지 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어있습니다."));
            }

            // 파일 크기 제한 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "파일 크기는 5MB 이하여야 합니다."));
            }

            // 파일 형식 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
            }

            // [1. 저장 경로 준비 (없으면 생성)]
            String savePath = "C:/profile_images/";
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            // [2. 파일명 생성 - (유저번호_타임스탬프_원본이름)]
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.lastIndexOf(".") != -1) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String safeName = originalName != null ? originalName.replaceAll("[^a-zA-Z0-9가-힣_.-]", "_") : "";
            String fileName = userNo + "_" + System.currentTimeMillis() + "_" + safeName;
            // (파일명에 한글, 공백, 특수문자 있으면 _ 로 대체)

            // [3. 파일 저장]
            File dest = new File(savePath + fileName);
            file.transferTo(dest);

            // [4. DB에는 "/profile_images/파일명" 만 저장하도록 반환]
            String imageUrl = "/profile_images/" + fileName;
            // 필요하다면 userService.updateProfileImagePath(userNo, imageUrl); 호출해서 DB 저장

            log.info(" 프로필 이미지 업로드 성공: userId={}, imageUrl={}", userId, imageUrl);
            return ResponseEntity.ok(Map.of("imagePath", imageUrl));
        } catch (IOException e) {
            log.error(" 이미지 저장 중 IOException 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 저장 중 오류가 발생했습니다."));
        } catch (Exception e) {
            log.error(" 프로필 이미지 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 업로드 중 오류가 발생했습니다."));
        }
    }
}