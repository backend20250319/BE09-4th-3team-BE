package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.AddressRequestDTO;
import io.fundy.fundyserver.register.dto.AddressResponseDTO;
import io.fundy.fundyserver.register.dto.oauth.SessionUser;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.security.CustomOAuthUserDetails;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import io.fundy.fundyserver.register.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/user/me/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    // 배송지 목록 조회
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAddresses(@AuthenticationPrincipal Object principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            List<AddressResponseDTO> addresses;

            if (principal instanceof CustomOAuthUserDetails customOAuthUserDetails) {
                OAuthUser oauthUser = customOAuthUserDetails.getOAuthUser();
                addresses = addressService.getAddressesByOAuthUserId(oauthUser.getOauthId());
                log.info("OAuth 사용자 배송지 목록 조회: oauthId={}, count={}", oauthUser.getOauthId(), addresses.size());
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                addresses = addressService.getAddressesByUserNo(user.getUserNo());
                log.info("일반 사용자 배송지 목록 조회: userNo={}, count={}", user.getUserNo(), addresses.size());
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

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            AddressResponseDTO savedAddress;

            if (principal instanceof CustomOAuthUserDetails customOAuthUserDetails) {
                OAuthUser oauthUser = customOAuthUserDetails.getOAuthUser();
                savedAddress = addressService.addAddressByOAuthUser(oauthUser.getOauthId(), request);
                log.info("OAuth 사용자 배송지 등록 완료: oauthId={}, addressId={}", oauthUser.getOauthId(), savedAddress.getId());
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                savedAddress = addressService.addAddress(user.getUserNo(), request);
                log.info("일반 사용자 배송지 등록 완료: userNo={}, addressId={}", user.getUserNo(), savedAddress.getId());
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
        } catch (Exception e) {
            log.error("배송지 등록 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "배송지 등록에 실패했습니다.");
        }
    }

    // 배송지 삭제
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long addressId) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            if (principal instanceof CustomOAuthUserDetails customOAuthUserDetails) {
                OAuthUser oauthUser = customOAuthUserDetails.getOAuthUser();
                addressService.deleteAddressByOAuthUser(oauthUser.getOauthId(), addressId);
                log.info("OAuth 사용자 배송지 삭제 완료: oauthId={}, addressId={}", oauthUser.getOauthId(), addressId);
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                addressService.deleteAddress(user.getUserNo(), addressId);
                log.info("일반 사용자 배송지 삭제 완료: userNo={}, addressId={}", user.getUserNo(), addressId);
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("배송지 삭제 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "배송지 삭제에 실패했습니다.");
        }
    }

    // 기본 배송지 설정
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long addressId) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            if (principal instanceof CustomOAuthUserDetails customOAuthUserDetails) {
                OAuthUser oauthUser = customOAuthUserDetails.getOAuthUser();
                addressService.setDefaultAddressByOAuthUser(oauthUser.getOauthId(), addressId);
                log.info("OAuth 사용자 기본 배송지 설정 완료: oauthId={}, addressId={}", oauthUser.getOauthId(), addressId);
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                addressService.setDefaultAddress(user.getUserNo(), addressId);
                log.info("일반 사용자 기본 배송지 설정 완료: userNo={}, addressId={}", user.getUserNo(), addressId);
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("기본 배송지 설정 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "기본 배송지 설정에 실패했습니다.");
        }
    }

    // 기본 배송지 조회
    @GetMapping("/default")
    public ResponseEntity<AddressResponseDTO> getDefaultAddress(@AuthenticationPrincipal Object principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인한 유저 정보가 없습니다.");
        }

        try {
            AddressResponseDTO defaultAddress;

            if (principal instanceof CustomOAuthUserDetails customOAuthUserDetails) {
                OAuthUser oauthUser = customOAuthUserDetails.getOAuthUser();
                defaultAddress = addressService.getDefaultAddressByOAuthUser(oauthUser.getOauthId());
                log.info("OAuth 사용자 기본 배송지 조회: oauthId={}", oauthUser.getOauthId());
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                defaultAddress = addressService.getDefaultAddress(user.getUserNo());
                log.info("일반 사용자 기본 배송지 조회: userNo={}", user.getUserNo());
            } else {
                log.error("알 수 없는 인증 주체: {}", principal.getClass());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "알 수 없는 인증 주체입니다.");
            }

            if (defaultAddress == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(defaultAddress);
        } catch (Exception e) {
            log.error("기본 배송지 조회 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "기본 배송지 조회에 실패했습니다.");
        }
    }
}