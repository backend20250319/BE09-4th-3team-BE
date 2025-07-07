package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.AddressRequestDTO;
import io.fundy.fundyserver.register.dto.AddressResponseDTO;
import io.fundy.fundyserver.register.entity.Address;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.repository.AddressRepository;
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

    // ✅ 배송지 등록
    @Transactional
    public AddressResponseDTO addAddress(Integer userNo, AddressRequestDTO req) {
        log.info("✅ 배송지 등록 시작: userNo={}, recipientName={}", userNo, req.getName());

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 기본 배송지로 설정하는 경우, 기존 기본 배송지 해제
        if (req.isDefault()) {
            addressRepository.clearDefaultAddress(userNo);
            log.info("✅ 기존 기본 배송지 해제 완료: userNo={}", userNo);
        }

        Address address = Address.builder()
                .user(user)
                .name(req.getName())
                .phone(req.getPhone())
                .zipcode(req.getZipcode())
                .address(req.getAddress())
                .detail(req.getDetail())
                .isDefault(req.isDefault())
                .build();

        Address saved = addressRepository.save(address);
        log.info("✅ 배송지 등록 완료: addressId={}", saved.getId());

        return toResponse(saved);
    }

    // ✅ 배송지 목록 조회
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAddressesByUserNo(Integer userNo) {
        log.info("✅ 배송지 목록 조회 시작: userNo={}", userNo);

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<AddressResponseDTO> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        log.info("✅ 배송지 목록 조회 완료: userNo={}, count={}", userNo, addresses.size());
        return addresses;
    }

    // ✅ 배송지 삭제
    @Transactional
    public void deleteAddress(Integer userNo, Long addressId) {
        log.info("✅ 배송지 삭제 시작: userNo={}, addressId={}", userNo, addressId);

        Address address = addressRepository.findByIdAndUserUserNo(addressId, userNo)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));

        addressRepository.delete(address);
        log.info("✅ 배송지 삭제 완료: addressId={}", addressId);
    }

    // ✅ 기본 배송지 설정
    @Transactional
    public void setDefaultAddress(Integer userNo, Long addressId) {
        log.info("✅ 기본 배송지 설정 시작: userNo={}, addressId={}", userNo, addressId);

        // 기존 기본 배송지 해제
        addressRepository.clearDefaultAddress(userNo);
        log.info("✅ 기존 기본 배송지 해제 완료: userNo={}", userNo);

        // 새로운 기본 배송지 설정
        Address address = addressRepository.findByIdAndUserUserNo(addressId, userNo)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));

        address.setIsDefault(true);
        addressRepository.save(address);
        log.info("✅ 기본 배송지 설정 완료: addressId={}", addressId);
    }

    // ✅ 배송지 수정
    @Transactional
    public AddressResponseDTO updateAddress(Integer userNo, Long addressId, AddressRequestDTO req) {
        log.info("✅ 배송지 수정 시작: userNo={}, addressId={}", userNo, addressId);

        Address address = addressRepository.findByIdAndUserUserNo(addressId, userNo)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));

        // 기본 배송지로 변경하는 경우, 기존 기본 배송지 해제
        if (req.isDefault() && !address.getIsDefault()) {
            addressRepository.clearDefaultAddress(userNo);
            log.info("✅ 기존 기본 배송지 해제 완료: userNo={}", userNo);
        }

        // 배송지 정보 업데이트
        address.setName(req.getName());
        address.setPhone(req.getPhone());
        address.setZipcode(req.getZipcode());
        address.setAddress(req.getAddress());
        address.setDetail(req.getDetail());
        address.setIsDefault(req.isDefault());

        Address updated = addressRepository.save(address);
        log.info("✅ 배송지 수정 완료: addressId={}", addressId);

        return toResponse(updated);
    }

    // ✅ 기본 배송지 조회
    @Transactional(readOnly = true)
    public AddressResponseDTO getDefaultAddress(Integer userNo) {
        log.info("✅ 기본 배송지 조회 시작: userNo={}", userNo);

        Address defaultAddress = addressRepository.findByUserUserNoAndIsDefaultTrue(userNo)
                .orElse(null);

        if (defaultAddress == null) {
            log.info("✅ 기본 배송지 없음: userNo={}", userNo);
            return null;
        }

        log.info("✅ 기본 배송지 조회 완료: addressId={}", defaultAddress.getId());
        return toResponse(defaultAddress);
    }

    // ✅ 배송지 개수 조회
    @Transactional(readOnly = true)
    public long getAddressCount(Integer userNo) {
        return addressRepository.countByUserUserNo(userNo);
    }

    // ✅ Address → AddressResponseDTO 변환
    private AddressResponseDTO toResponse(Address address) {
        return AddressResponseDTO.builder()
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
    }
}