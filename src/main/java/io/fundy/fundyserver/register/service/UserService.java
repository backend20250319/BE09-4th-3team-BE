package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.PasswordChangeRequestDTO;
import io.fundy.fundyserver.register.dto.UserRequestDTO;
import io.fundy.fundyserver.register.dto.UserResponseDTO;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.entity.UserStatus;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserResponseDTO signup(UserRequestDTO req) {
        if (userRepository.existsByUserId(req.getUserId())) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_ID);
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(req.getNickname())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .userId(req.getUserId())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .phone(req.getPhone())
                .address(req.getAddress())
                .accountNumber(req.getAccountNumber())
                .userStatus(UserStatus.LOGOUT)
                .roleType(RoleType.USER)
                .build();

        userRepository.save(user);
        return toResponse(user);
    }

    // 로그인
    @Transactional
    public UserResponseDTO login(String userId, String rawPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserStatus() == UserStatus.BANNED) {
            throw new ApiException(ErrorCode.BANNED_USER);
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD);
        }

        user.setUserStatus(UserStatus.LOGIN);
        user.setLastLoginAt(LocalDateTime.now());  // ✅ 로그인 시간 기록
        userRepository.save(user);
        return toResponse(user);
    }

    // 로그아웃
    @Transactional
    public void logout(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        user.setUserStatus(UserStatus.LOGOUT);
        user.setLastLogoutAt(LocalDateTime.now()); // ✅ 로그아웃 시간 기록
        userRepository.save(user);
    }

    // 프로필 조회
    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfile(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return toResponse(user);
    }

    // 회원 정보 수정
    @Transactional
    public UserResponseDTO updateUser(Integer id, UserRequestDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!user.getUserId().equals(req.getUserId()) && userRepository.existsByUserId(req.getUserId())) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_ID);
        }
        if (!user.getEmail().equals(req.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (!user.getNickname().equals(req.getNickname()) && userRepository.existsByNickname(req.getNickname())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.setUserId(req.getUserId());
        user.setEmail(req.getEmail());
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setAddress(req.getAddress());
        user.setAccountNumber(req.getAccountNumber());
        userRepository.save(user);
        return toResponse(user);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Integer id, PasswordChangeRequestDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.PASSWORD_MISMATCH);
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // 상태 변경 (회원 탈퇴 등)
    @Transactional
    public void updateUserStatus(Integer id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.setUserStatus(status);
        userRepository.save(user);
    }

    // ID로 유저 조회
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    // userId로 유저 조회
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return toResponse(user);
    }

    // Entity → DTO 변환
    private UserResponseDTO toResponse(User u) {
        return UserResponseDTO.builder()
                .userNo(u.getUserNo())
                .userId(u.getUserId())
                .nickname(u.getNickname())
                .email(u.getEmail())
                .phone(u.getPhone())
                .address(u.getAddress())
                .accountNumber(u.getAccountNumber())
                .userStatus(u.getUserStatus())
                .roleType(u.getRoleType())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .lastLogoutAt(u.getLastLogoutAt())
                .build();
    }
}
