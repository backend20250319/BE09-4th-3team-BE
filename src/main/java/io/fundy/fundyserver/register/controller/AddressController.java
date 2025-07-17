package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.AddressRequestDTO;
import io.fundy.fundyserver.register.dto.AddressResponseDTO;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.OAuthUserRepository;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import io.fundy.fundyserver.register.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/me/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;
    private final OAuthUserRepository oAuthUserRepository;

    // 배송지 목록 조회
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAddresses(@AuthenticationPrincipal Object principal) {
        log.info("=== 배송지 목록 조회 시작 ===");
        log.info("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            List<AddressResponseDTO> addresses;

            if (principal instanceof DefaultOAuth2User oAuth2User) {
                String email = (String) oAuth2User.getAttribute("email");
                log.info("OAuth 사용자 이메일: {}", email);
                OAuthUser oauthUser = oAuthUserRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OAuth 사용자를 찾을 수 없습니다."));
                addresses = addressService.getAddressesByOAuthUserId(oauthUser.getOauthId());
                log.info("OAuth 사용자 배송지 목록 조회 완료: oauthId={}, count={}", oauthUser.getOauthId(), addresses.size());
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                log.info("일반 사용자 정보: userNo={}, userId={}", user.getUserNo(), user.getUserId());
                addresses = addressService.getAddressesByUserNo(user.getUserNo());
                log.info("일반 사용자 배송지 목록 조회 완료: userNo={}, count={}", user.getUserNo(), addresses.size());
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("배송지 목록 조회 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "배송지 목록 조회에 실패했습니다.");
        }
    }

    // 배송지 등록
    @PostMapping
    public ResponseEntity<AddressResponseDTO> addAddress(
            @AuthenticationPrincipal Object principal,
            @RequestBody AddressRequestDTO request) {

        log.info("=== 배송지 등록 시작 ===");
        log.info("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");
        log.info("Request data: {}", request);

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            AddressResponseDTO savedAddress;

            if (principal instanceof DefaultOAuth2User oAuth2User) {
                String email = (String) oAuth2User.getAttribute("email");
                log.info("OAuth 사용자 이메일: {}", email);
                OAuthUser oauthUser = oAuthUserRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OAuth 사용자를 찾을 수 없습니다."));
                savedAddress = addressService.addAddressByOAuthUser(oauthUser.getOauthId(), request);
                log.info("OAuth 사용자 배송지 등록 완료: oauthId={}, addressId={}", oauthUser.getOauthId(), savedAddress.getId());
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                log.info("일반 사용자 정보: userNo={}, userId={}", user.getUserNo(), user.getUserId());
                savedAddress = addressService.addAddress(user.getUserNo(), request);
                log.info("일반 사용자 배송지 등록 완료: userNo={}, addressId={}", user.getUserNo(), savedAddress.getId());
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
        } catch (Exception e) {
            log.error("배송지 등록 실패", e);
            log.error("오류 상세: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "배송지 등록에 실패했습니다.");
        }
    }

    // 배송지 삭제
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long addressId) {

        log.info("=== 배송지 삭제 시작 ===");
        log.info("Address ID: {}", addressId);
        log.info("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            if (principal instanceof DefaultOAuth2User oAuth2User) {
                String email = (String) oAuth2User.getAttribute("email");
                log.info("OAuth 사용자 이메일: {}", email);
                OAuthUser oauthUser = oAuthUserRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OAuth 사용자를 찾을 수 없습니다."));
                addressService.deleteAddressByOAuthUser(oauthUser.getOauthId(), addressId);
                log.info("OAuth 사용자 배송지 삭제 완료: oauthId={}, addressId={}", oauthUser.getOauthId(), addressId);
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                log.info("일반 사용자 정보: userNo={}, userId={}", user.getUserNo(), user.getUserId());
                addressService.deleteAddress(user.getUserNo(), addressId);
                log.info("일반 사용자 배송지 삭제 완료: userNo={}, addressId={}", user.getUserNo(), addressId);
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("배송지 삭제 실패", e);
            log.error("오류 상세: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "배송지 삭제에 실패했습니다.");
        }
    }

    // 기본 배송지 설정
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long addressId) {

        log.info("=== 기본 배송지 설정 시작 ===");
        log.info("Address ID: {}", addressId);
        log.info("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            if (principal instanceof DefaultOAuth2User oAuth2User) {
                String email = (String) oAuth2User.getAttribute("email");
                log.info("OAuth 사용자 이메일: {}", email);
                OAuthUser oauthUser = oAuthUserRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OAuth 사용자를 찾을 수 없습니다."));
                addressService.setDefaultAddressByOAuthUser(oauthUser.getOauthId(), addressId);
                log.info("OAuth 사용자 기본 배송지 설정 완료: oauthId={}, addressId={}", oauthUser.getOauthId(), addressId);
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                log.info("일반 사용자 정보: userNo={}, userId={}", user.getUserNo(), user.getUserId());
                addressService.setDefaultAddress(user.getUserNo(), addressId);
                log.info("일반 사용자 기본 배송지 설정 완료: userNo={}, addressId={}", user.getUserNo(), addressId);
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("기본 배송지 설정 실패", e);
            log.error("오류 상세: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "기본 배송지 설정에 실패했습니다.");
        }
    }

    // 기본 배송지 조회
    @GetMapping("/default")
    public ResponseEntity<AddressResponseDTO> getDefaultAddress(@AuthenticationPrincipal Object principal) {
        log.info("=== 기본 배송지 조회 시작 ===");
        log.info("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            AddressResponseDTO defaultAddress;

            if (principal instanceof DefaultOAuth2User oAuth2User) {
                String email = (String) oAuth2User.getAttribute("email");
                log.info("OAuth 사용자 이메일: {}", email);
                OAuthUser oauthUser = oAuthUserRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OAuth 사용자를 찾을 수 없습니다."));
                defaultAddress = addressService.getDefaultAddressByOAuthUser(oauthUser.getOauthId());
                log.info("OAuth 사용자 기본 배송지 조회 완료: oauthId={}", oauthUser.getOauthId());
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                log.info("일반 사용자 정보: userNo={}, userId={}", user.getUserNo(), user.getUserId());
                defaultAddress = addressService.getDefaultAddress(user.getUserNo());
                log.info("일반 사용자 기본 배송지 조회 완료: userNo={}", user.getUserNo());
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            if (defaultAddress == null) {
                log.info("기본 배송지가 없습니다.");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(defaultAddress);
        } catch (Exception e) {
            log.error("기본 배송지 조회 실패", e);
            log.error("오류 상세: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "기본 배송지 조회에 실패했습니다.");
        }
    }
}