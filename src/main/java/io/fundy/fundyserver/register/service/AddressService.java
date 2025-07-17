package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.AddressRequestDTO;
import io.fundy.fundyserver.register.dto.AddressResponseDTO;
import io.fundy.fundyserver.register.entity.Address;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.repository.AddressRepository;
import io.fundy.fundyserver.register.repository.OAuthUserRepository;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OAuthUserRepository oAuthUserRepository;

    // === 일반 사용자 관련 메서드 ===

    // 배송지 등록
    @Transactional
    public AddressResponseDTO addAddress(Integer userNo, AddressRequestDTO req) {
        try {
            log.info("=== 배송지 등록 시작 ===");
            log.info("userNo: {}", userNo);
            log.info("요청 데이터: name={}, phone={}, zipcode={}, address={}, detail={}, isDefault={}",
                    req.getName(), req.getPhone(), req.getZipcode(), req.getAddress(), req.getDetail(), req.isDefault());

            // 사용자 조회
            User user = userRepository.findById(userNo)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음: userNo={}", userNo);
                        return new ApiException(ErrorCode.USER_NOT_FOUND);
                    });
            log.info("사용자 조회 성공: userNo={}, email={}", user.getUserNo(), user.getEmail());

            // 기본 배송지로 설정하는 경우, 기존 기본 배송지 해제
            if (req.isDefault()) {
                log.info("기본 배송지로 설정 요청 - 기존 기본 배송지 해제 시작");
                addressRepository.clearDefaultAddress(userNo);
                log.info("기존 기본 배송지 해제 완료: userNo={}", userNo);
            }

            // Address 엔티티 생성
            log.info("Address 엔티티 생성 시작");
            Address address = Address.builder()
                    .user(user)
                    .name(req.getName())
                    .phone(req.getPhone())
                    .zipcode(req.getZipcode())
                    .address(req.getAddress())
                    .detail(req.getDetail())
                    .isDefault(req.isDefault())
                    .build();
            log.info("Address 엔티티 생성 완료: {}", address);

            // 데이터베이스 저장
            log.info("데이터베이스 저장 시작");
            Address saved = addressRepository.save(address);
            log.info("데이터베이스 저장 완료: addressId={}", saved.getId());

            // 응답 DTO 생성
            AddressResponseDTO response = toResponse(saved);
            log.info("응답 DTO 생성 완료: {}", response);
            log.info("=== 배송지 등록 완료 ===");

            return response;

        } catch (ApiException e) {
            log.error("배송지 등록 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("배송지 등록 중 예상치 못한 오류 발생: userNo={}, error={}", userNo, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 배송지 목록 조회
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAddressesByUserNo(Integer userNo) {
        try {
            log.info("=== 배송지 목록 조회 시작: userNo={} ===", userNo);

            User user = userRepository.findById(userNo)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음: userNo={}", userNo);
                        return new ApiException(ErrorCode.USER_NOT_FOUND);
                    });
            log.info("사용자 조회 성공: userNo={}, email={}", user.getUserNo(), user.getEmail());

            List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            log.info("데이터베이스에서 배송지 조회 완료: count={}", addresses.size());

            List<AddressResponseDTO> response = addresses.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            log.info("=== 배송지 목록 조회 완료: userNo={}, count={} ===", userNo, response.size());
            return response;

        } catch (ApiException e) {
            log.error("배송지 목록 조회 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("배송지 목록 조회 중 예상치 못한 오류 발생: userNo={}, error={}", userNo, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 배송지 삭제
    @Transactional
    public void deleteAddress(Integer userNo, Long addressId) {
        try {
            log.info("=== 배송지 삭제 시작: userNo={}, addressId={} ===", userNo, addressId);

            Address address = addressRepository.findByIdAndUserUserNo(addressId, userNo)
                    .orElseThrow(() -> {
                        log.error("배송지를 찾을 수 없음: addressId={}, userNo={}", addressId, userNo);
                        return new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
                    });
            log.info("삭제할 배송지 조회 성공: addressId={}", addressId);

            addressRepository.delete(address);
            log.info("=== 배송지 삭제 완료: addressId={} ===", addressId);

        } catch (ApiException e) {
            log.error("배송지 삭제 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("배송지 삭제 중 예상치 못한 오류 발생: userNo={}, addressId={}, error={}", userNo, addressId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 기본 배송지 설정
    @Transactional
    public void setDefaultAddress(Integer userNo, Long addressId) {
        try {
            log.info("=== 기본 배송지 설정 시작: userNo={}, addressId={} ===", userNo, addressId);

            // 기존 기본 배송지 해제
            addressRepository.clearDefaultAddress(userNo);
            log.info("기존 기본 배송지 해제 완료: userNo={}", userNo);

            // 새로운 기본 배송지 설정
            Address address = addressRepository.findByIdAndUserUserNo(addressId, userNo)
                    .orElseThrow(() -> {
                        log.error("배송지를 찾을 수 없음: addressId={}, userNo={}", addressId, userNo);
                        return new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
                    });
            log.info("기본 배송지로 설정할 배송지 조회 성공: addressId={}", addressId);

            address.setIsDefault(true);
            addressRepository.save(address);
            log.info("=== 기본 배송지 설정 완료: addressId={} ===", addressId);

        } catch (ApiException e) {
            log.error("기본 배송지 설정 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("기본 배송지 설정 중 예상치 못한 오류 발생: userNo={}, addressId={}, error={}", userNo, addressId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 배송지 수정
    @Transactional
    public AddressResponseDTO updateAddress(Integer userNo, Long addressId, AddressRequestDTO req) {
        try {
            log.info("=== 배송지 수정 시작: userNo={}, addressId={} ===", userNo, addressId);
            log.info("수정 요청 데이터: name={}, phone={}, zipcode={}, address={}, detail={}, isDefault={}",
                    req.getName(), req.getPhone(), req.getZipcode(), req.getAddress(), req.getDetail(), req.isDefault());

            Address address = addressRepository.findByIdAndUserUserNo(addressId, userNo)
                    .orElseThrow(() -> {
                        log.error("배송지를 찾을 수 없음: addressId={}, userNo={}", addressId, userNo);
                        return new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
                    });
            log.info("수정할 배송지 조회 성공: addressId={}, 현재 isDefault={}", addressId, address.getIsDefault());

            // 기본 배송지로 변경하는 경우, 기존 기본 배송지 해제
            if (req.isDefault() && !address.getIsDefault()) {
                log.info("기본 배송지로 변경 요청 - 기존 기본 배송지 해제 시작");
                addressRepository.clearDefaultAddress(userNo);
                log.info("기존 기본 배송지 해제 완료: userNo={}", userNo);
            }

            // 배송지 정보 업데이트
            log.info("배송지 정보 업데이트 시작");
            address.setName(req.getName());
            address.setPhone(req.getPhone());
            address.setZipcode(req.getZipcode());
            address.setAddress(req.getAddress());
            address.setDetail(req.getDetail());
            address.setIsDefault(req.isDefault());

            Address updated = addressRepository.save(address);
            log.info("배송지 정보 업데이트 완료: addressId={}", addressId);

            AddressResponseDTO response = toResponse(updated);
            log.info("=== 배송지 수정 완료: addressId={} ===", addressId);

            return response;

        } catch (ApiException e) {
            log.error("배송지 수정 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("배송지 수정 중 예상치 못한 오류 발생: userNo={}, addressId={}, error={}", userNo, addressId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 기본 배송지 조회
    @Transactional(readOnly = true)
    public AddressResponseDTO getDefaultAddress(Integer userNo) {
        try {
            log.info("=== 기본 배송지 조회 시작: userNo={} ===", userNo);

            Address defaultAddress = addressRepository.findByUserUserNoAndIsDefaultTrue(userNo)
                    .orElse(null);

            if (defaultAddress == null) {
                log.info("기본 배송지 없음: userNo={}", userNo);
                return null;
            }

            AddressResponseDTO response = toResponse(defaultAddress);
            log.info("=== 기본 배송지 조회 완료: addressId={} ===", defaultAddress.getId());
            return response;

        } catch (Exception e) {
            log.error("기본 배송지 조회 중 오류 발생: userNo={}, error={}", userNo, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 배송지 개수 조회
    @Transactional(readOnly = true)
    public long getAddressCount(Integer userNo) {
        try {
            log.info("배송지 개수 조회: userNo={}", userNo);
            long count = addressRepository.countByUserUserNo(userNo);
            log.info("배송지 개수 조회 완료: userNo={}, count={}", userNo, count);
            return count;
        } catch (Exception e) {
            log.error("배송지 개수 조회 중 오류 발생: userNo={}, error={}", userNo, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // === OAuth 사용자 관련 메서드 ===

    // OAuth 사용자 배송지 등록
    @Transactional
    public AddressResponseDTO addAddressByOAuthUser(Long oauthId, AddressRequestDTO req) {
        try {
            log.info("=== OAuth 사용자 배송지 등록 시작 ===");
            log.info("oauthId: {}", oauthId);
            log.info("요청 데이터: name={}, phone={}, zipcode={}, address={}, detail={}, isDefault={}",
                    req.getName(), req.getPhone(), req.getZipcode(), req.getAddress(), req.getDetail(), req.isDefault());

            // OAuth 사용자 조회
            OAuthUser oauthUser = oAuthUserRepository.findById(oauthId)
                    .orElseThrow(() -> {
                        log.error("OAuth 사용자를 찾을 수 없음: oauthId={}", oauthId);
                        return new ApiException(ErrorCode.USER_NOT_FOUND);
                    });
            log.info("OAuth 사용자 조회 성공: oauthId={}, email={}", oauthUser.getOauthId(), oauthUser.getEmail());

            // 기본 배송지로 설정하는 경우, 기존 기본 배송지 해제
            if (req.isDefault()) {
                log.info("OAuth 사용자 기본 배송지로 설정 요청 - 기존 기본 배송지 해제 시작");
                addressRepository.clearDefaultAddressByOAuthUser(oauthId);
                log.info("OAuth 사용자 기존 기본 배송지 해제 완료: oauthId={}", oauthId);
            }

            // Address 엔티티 생성
            log.info("OAuth 사용자 Address 엔티티 생성 시작");
            Address address = Address.builder()
                    .oauthUser(oauthUser)
                    .name(req.getName())
                    .phone(req.getPhone())
                    .zipcode(req.getZipcode())
                    .address(req.getAddress())
                    .detail(req.getDetail())
                    .isDefault(req.isDefault())
                    .build();
            log.info("OAuth 사용자 Address 엔티티 생성 완료: {}", address);

            // 데이터베이스 저장
            log.info("OAuth 사용자 데이터베이스 저장 시작");
            Address saved = addressRepository.save(address);
            log.info("OAuth 사용자 데이터베이스 저장 완료: addressId={}", saved.getId());

            // 응답 DTO 생성
            AddressResponseDTO response = toResponse(saved);
            log.info("OAuth 사용자 응답 DTO 생성 완료: {}", response);
            log.info("=== OAuth 사용자 배송지 등록 완료 ===");

            return response;

        } catch (ApiException e) {
            log.error("OAuth 사용자 배송지 등록 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OAuth 사용자 배송지 등록 중 예상치 못한 오류 발생: oauthId={}, error={}", oauthId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // OAuth 사용자 배송지 목록 조회
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAddressesByOAuthUserId(Long oauthId) {
        try {
            log.info("=== OAuth 사용자 배송지 목록 조회 시작: oauthId={} ===", oauthId);

            OAuthUser oauthUser = oAuthUserRepository.findById(oauthId)
                    .orElseThrow(() -> {
                        log.error("OAuth 사용자를 찾을 수 없음: oauthId={}", oauthId);
                        return new ApiException(ErrorCode.USER_NOT_FOUND);
                    });
            log.info("OAuth 사용자 조회 성공: oauthId={}, email={}", oauthUser.getOauthId(), oauthUser.getEmail());

            List<Address> addresses = addressRepository.findByOauthUserOrderByIsDefaultDescCreatedAtDesc(oauthUser);
            log.info("OAuth 사용자 데이터베이스에서 배송지 조회 완료: count={}", addresses.size());

            List<AddressResponseDTO> response = addresses.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            log.info("=== OAuth 사용자 배송지 목록 조회 완료: oauthId={}, count={} ===", oauthId, response.size());
            return response;

        } catch (ApiException e) {
            log.error("OAuth 사용자 배송지 목록 조회 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OAuth 사용자 배송지 목록 조회 중 예상치 못한 오류 발생: oauthId={}, error={}", oauthId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // OAuth 사용자 배송지 삭제
    @Transactional
    public void deleteAddressByOAuthUser(Long oauthId, Long addressId) {
        try {
            log.info("=== OAuth 사용자 배송지 삭제 시작: oauthId={}, addressId={} ===", oauthId, addressId);

            Address address = addressRepository.findByIdAndOauthUserOauthId(addressId, oauthId)
                    .orElseThrow(() -> {
                        log.error("OAuth 사용자 배송지를 찾을 수 없음: addressId={}, oauthId={}", addressId, oauthId);
                        return new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
                    });
            log.info("OAuth 사용자 삭제할 배송지 조회 성공: addressId={}", addressId);

            addressRepository.delete(address);
            log.info("=== OAuth 사용자 배송지 삭제 완료: addressId={} ===", addressId);

        } catch (ApiException e) {
            log.error("OAuth 사용자 배송지 삭제 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OAuth 사용자 배송지 삭제 중 예상치 못한 오류 발생: oauthId={}, addressId={}, error={}", oauthId, addressId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // OAuth 사용자 기본 배송지 설정
    @Transactional
    public void setDefaultAddressByOAuthUser(Long oauthId, Long addressId) {
        try {
            log.info("=== OAuth 사용자 기본 배송지 설정 시작: oauthId={}, addressId={} ===", oauthId, addressId);

            // 기존 기본 배송지 해제
            addressRepository.clearDefaultAddressByOAuthUser(oauthId);
            log.info("OAuth 사용자 기존 기본 배송지 해제 완료: oauthId={}", oauthId);

            // 새로운 기본 배송지 설정
            Address address = addressRepository.findByIdAndOauthUserOauthId(addressId, oauthId)
                    .orElseThrow(() -> {
                        log.error("OAuth 사용자 배송지를 찾을 수 없음: addressId={}, oauthId={}", addressId, oauthId);
                        return new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
                    });
            log.info("OAuth 사용자 기본 배송지로 설정할 배송지 조회 성공: addressId={}", addressId);

            address.setIsDefault(true);
            addressRepository.save(address);
            log.info("=== OAuth 사용자 기본 배송지 설정 완료: addressId={} ===", addressId);

        } catch (ApiException e) {
            log.error("OAuth 사용자 기본 배송지 설정 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OAuth 사용자 기본 배송지 설정 중 예상치 못한 오류 발생: oauthId={}, addressId={}, error={}", oauthId, addressId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // OAuth 사용자 배송지 수정
    @Transactional
    public AddressResponseDTO updateAddressByOAuthUser(Long oauthId, Long addressId, AddressRequestDTO req) {
        try {
            log.info("=== OAuth 사용자 배송지 수정 시작: oauthId={}, addressId={} ===", oauthId, addressId);
            log.info("OAuth 사용자 수정 요청 데이터: name={}, phone={}, zipcode={}, address={}, detail={}, isDefault={}",
                    req.getName(), req.getPhone(), req.getZipcode(), req.getAddress(), req.getDetail(), req.isDefault());

            Address address = addressRepository.findByIdAndOauthUserOauthId(addressId, oauthId)
                    .orElseThrow(() -> {
                        log.error("OAuth 사용자 배송지를 찾을 수 없음: addressId={}, oauthId={}", addressId, oauthId);
                        return new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
                    });
            log.info("OAuth 사용자 수정할 배송지 조회 성공: addressId={}, 현재 isDefault={}", addressId, address.getIsDefault());

            // 기본 배송지로 변경하는 경우, 기존 기본 배송지 해제
            if (req.isDefault() && !address.getIsDefault()) {
                log.info("OAuth 사용자 기본 배송지로 변경 요청 - 기존 기본 배송지 해제 시작");
                addressRepository.clearDefaultAddressByOAuthUser(oauthId);
                log.info("OAuth 사용자 기존 기본 배송지 해제 완료: oauthId={}", oauthId);
            }

            // 배송지 정보 업데이트
            log.info("OAuth 사용자 배송지 정보 업데이트 시작");
            address.setName(req.getName());
            address.setPhone(req.getPhone());
            address.setZipcode(req.getZipcode());
            address.setAddress(req.getAddress());
            address.setDetail(req.getDetail());
            address.setIsDefault(req.isDefault());

            Address updated = addressRepository.save(address);
            log.info("OAuth 사용자 배송지 정보 업데이트 완료: addressId={}", addressId);

            AddressResponseDTO response = toResponse(updated);
            log.info("=== OAuth 사용자 배송지 수정 완료: addressId={} ===", addressId);

            return response;

        } catch (ApiException e) {
            log.error("OAuth 사용자 배송지 수정 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OAuth 사용자 배송지 수정 중 예상치 못한 오류 발생: oauthId={}, addressId={}, error={}", oauthId, addressId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // OAuth 사용자 기본 배송지 조회
    @Transactional(readOnly = true)
    public AddressResponseDTO getDefaultAddressByOAuthUser(Long oauthId) {
        try {
            log.info("=== OAuth 사용자 기본 배송지 조회 시작: oauthId={} ===", oauthId);

            OAuthUser oauthUser = oAuthUserRepository.findById(oauthId)
                    .orElseThrow(() -> {
                        log.error("OAuth 사용자를 찾을 수 없음: oauthId={}", oauthId);
                        return new ApiException(ErrorCode.USER_NOT_FOUND);
                    });
            log.info("OAuth 사용자 조회 성공: oauthId={}, email={}", oauthUser.getOauthId(), oauthUser.getEmail());

            Address defaultAddress = addressRepository.findByOauthUserAndIsDefaultTrue(oauthUser)
                    .orElse(null);

            if (defaultAddress == null) {
                log.info("OAuth 사용자 기본 배송지 없음: oauthId={}", oauthId);
                return null;
            }

            AddressResponseDTO response = toResponse(defaultAddress);
            log.info("=== OAuth 사용자 기본 배송지 조회 완료: addressId={} ===", defaultAddress.getId());
            return response;

        } catch (ApiException e) {
            log.error("OAuth 사용자 기본 배송지 조회 중 ApiException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OAuth 사용자 기본 배송지 조회 중 예상치 못한 오류 발생: oauthId={}, error={}", oauthId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // OAuth 사용자 배송지 개수 조회
    @Transactional(readOnly = true)
    public long getAddressCountByOAuthUser(Long oauthId) {
        try {
            log.info("OAuth 사용자 배송지 개수 조회: oauthId={}", oauthId);
            long count = addressRepository.countByOauthUserOauthId(oauthId);
            log.info("OAuth 사용자 배송지 개수 조회 완료: oauthId={}, count={}", oauthId, count);
            return count;
        } catch (Exception e) {
            log.error("OAuth 사용자 배송지 개수 조회 중 오류 발생: oauthId={}, error={}", oauthId, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // Address → AddressResponseDTO 변환
    private AddressResponseDTO toResponse(Address address) {
        try {
            AddressResponseDTO response = AddressResponseDTO.builder()
                    .id(address.getId())
                    .name(address.getName())
                    .phone(address.getPhone())
                    .zipcode(address.getZipcode())
                    .address(address.getAddress())
                    .detail(address.getDetail())
                    .isDefault(address.getIsDefault())
                    .createdAt(address.getCreatedAt())
                    .updatedAt(address.getUpdatedAt())
                    .build();

            log.debug("Address → AddressResponseDTO 변환 완료: addressId={}, name={}", address.getId(), address.getName());
            return response;

        } catch (Exception e) {
            log.error("Address → AddressResponseDTO 변환 중 오류 발생: addressId={}, error={}", address.getId(), e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}