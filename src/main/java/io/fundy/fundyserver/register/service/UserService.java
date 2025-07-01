package io.fundy.fundyserver.register.service;

import com.sun.jdi.ReferenceType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);  // logger 추가
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserResponseDTO signup(UserRequestDTO req) {
        try {
            // 아이디 중복 확인
            if (userRepository.existsByUserId(req.getUserId())) {
                logger.error("아이디 중복: {}", req.getUserId());
                throw new ApiException(ErrorCode.DUPLICATE_USER_ID);
            }

            // 이메일 중복 확인
            if (userRepository.existsByEmail(req.getEmail())) {
                logger.error("이메일 중복: {}", req.getEmail());
                throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
            }

            // 닉네임 중복 확인
            if (userRepository.existsByNickname(req.getNickname())) {
                logger.error("닉네임 중복: {}", req.getNickname());
                throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
            }

            // 비밀번호 암호화
            String encodedPassword;
            try {
                encodedPassword = passwordEncoder.encode(req.getPassword());
            } catch (Exception e) {
                logger.error("비밀번호 암호화 실패: ", e);
                throw new ApiException(ErrorCode.PASSWORD_ENCRYPTION_FAILED);
            }

            // 새로운 사용자 객체 생성
            User user = User.builder()
                    .userId(req.getUserId())
                    .email(req.getEmail())
                    .password(encodedPassword)
                    .nickname(req.getNickname())
                    .phone(req.getPhone())
                    .address(req.getAddress())
                    .addressDetail(req.getAddressDetail())
                    .userStatus(UserStatus.LOGOUT)
                    .roleType(RoleType.USER)
                    .build();

            // 데이터베이스에 사용자 저장
            try {
                userRepository.save(user);
            } catch (Exception e) {
                logger.error("사용자 저장 실패: ", e);
                throw new ApiException(ErrorCode.DATABASE_SAVE_ERROR);
            }

            // DTO 반환
            return toResponse(user);

        } catch (ApiException e) {
            // 이미 정의된 ApiException을 처리하여 에러 메시지 반환
            logger.error("회원가입 중 오류 발생: ", e);
            throw e;  // 예외를 다시 던져서 API 레벨에서 처리하도록 합니다.
        } catch (Exception e) {
            // 다른 예외를 처리하여 시스템 예외로 처리
            logger.error("회원가입 중 예기치 않은 오류 발생: ", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 아이디 중복 확인
    @Transactional(readOnly = true)
    public boolean isUserIdDuplicate(String userId) {
        boolean exists = userRepository.existsByUserId(userId);
        if (exists) {
            logger.error("중복된 아이디: {}", userId);
        }
        return exists;
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicate(String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        if (exists) {
            logger.error("중복된 닉네임: {}", nickname);
        }
        return exists;
    }

    // 이메일 중복 확인
    @Transactional(readOnly = true)
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    // 전화번호 중복 확인
    @Transactional(readOnly = true)
    public boolean isPhoneDuplicate(String phone) {
        boolean exists = userRepository.existsByPhone(phone);
        if (exists) {
            logger.error("중복된 전화번호: {}", phone);
        }
        return exists;
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

        // 중복된 사용자 정보 확인
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
        userRepository.save(user);
        return toResponse(user);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Integer id, PasswordChangeRequestDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
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
                .userStatus(u.getUserStatus())
                .roleType(u.getRoleType())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .lastLogoutAt(u.getLastLogoutAt())
                .build();
    }
    public User getUserEntityByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}
