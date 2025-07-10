package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.PasswordChangeRequestDTO;
import io.fundy.fundyserver.register.dto.UserRequestDTO;
import io.fundy.fundyserver.register.dto.UserResponseDTO;
import io.fundy.fundyserver.register.dto.UserUpdateRequestDTO;
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
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserResponseDTO signup(UserRequestDTO req) {
        try {
            if (userRepository.existsByUserId(req.getUserId())) {
                logger.error("아이디 중복: {}", req.getUserId());
                throw new ApiException(ErrorCode.DUPLICATE_USER_ID);
            }
            if (userRepository.existsByEmail(req.getEmail())) {
                logger.error("이메일 중복: {}", req.getEmail());
                throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
            }
            if (userRepository.existsByNickname(req.getNickname())) {
                logger.error("닉네임 중복: {}", req.getNickname());
                throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
            }
            String encodedPassword;
            try {
                encodedPassword = passwordEncoder.encode(req.getPassword());
            } catch (Exception e) {
                logger.error("비밀번호 암호화 실패: ", e);
                throw new ApiException(ErrorCode.PASSWORD_ENCRYPTION_FAILED);
            }
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
                    .profileImg(req.getProfileImg()) // ★ 필드 값 세팅
                    .build();
            try {
                userRepository.save(user);
            } catch (Exception e) {
                logger.error("사용자 저장 실패: ", e);
                throw new ApiException(ErrorCode.DATABASE_SAVE_ERROR);
            }
            return toResponse(user);

        } catch (ApiException e) {
            logger.error("회원가입 중 오류 발생: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("회원가입 중 예기치 않은 오류 발생: ", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 로그인 (강제 LOGOUT 후 로그인 허용)
    @Transactional
    public UserResponseDTO login(String userId, String rawPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserStatus() == UserStatus.BANNED) {
            throw new ApiException(ErrorCode.BANNED_USER);
        }
        if (user.getUserStatus() == UserStatus.LOGIN) {
            user.setUserStatus(UserStatus.LOGOUT);
            user.setLastLogoutAt(LocalDateTime.now());
            userRepository.save(user);
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD);
        }
        user.setUserStatus(UserStatus.LOGIN);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void logout(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.setUserStatus(UserStatus.LOGOUT);
        user.setLastLogoutAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean isUserIdDuplicate(String userId) {
        boolean exists = userRepository.existsByUserId(userId);
        if (exists) logger.error("중복된 아이디: {}", userId);
        return exists;
    }

    @Transactional(readOnly = true)
    public boolean isNicknameDuplicate(String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        if (exists) logger.error("중복된 닉네임: {}", nickname);
        return exists;
    }

    @Transactional(readOnly = true)
    public boolean isEmailDuplicate(String email) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) logger.error("중복된 이메일: {}", email);
        return exists;
    }

    @Transactional(readOnly = true)
    public boolean isPhoneDuplicate(String phone) {
        boolean exists = userRepository.existsByPhone(phone);
        if (exists) logger.error("중복된 전화번호: {}", phone);
        return exists;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfile(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return toResponse(user);
    }

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
        user.setAddressDetail(req.getAddressDetail());

        // profileImg가 들어왔다면 수정
        if (req.getProfileImg() != null && !req.getProfileImg().isBlank()) {
            user.setProfileImg(req.getProfileImg());
        }

        userRepository.save(user);
        return toResponse(user);
    }

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

    @Transactional
    public void updateUserStatus(Integer id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.setUserStatus(status);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntityByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    // 회원정보 수정 시 프로필 이미지 null 덮어쓰기 방지
    @Transactional
    public UserResponseDTO updateUserProfile(Integer id, UserUpdateRequestDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equals(req.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (!user.getNickname().equals(req.getNickname()) && userRepository.existsByNickname(req.getNickname())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 프로필 이미지(이미지 값이 있을 때만 변경)
        if (req.getProfileImg() != null && !req.getProfileImg().isBlank()) {
            user.setProfileImg(req.getProfileImg());
        }

        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setAddress(req.getAddress());
        user.setAddressDetail(req.getAddressDetail());

        userRepository.save(user);
        return toResponse(user);
    }

    // 프로필 이미지 업로드
    @Transactional
    public String uploadProfileImage(Integer userNo, MultipartFile file) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        String uploadDir = "C:/profile_images/"; // 서버 내 실제 경로로 수정
        String originalFilename = file.getOriginalFilename();
        String fileName = userNo + "_" + System.currentTimeMillis() + "_" + originalFilename;
        java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, fileName);
        try {
            java.nio.file.Files.createDirectories(filePath.getParent());
            file.transferTo(filePath);

            String imageUrl = "/profile_images/" + fileName;
            user.setProfileImg(imageUrl);
            userRepository.save(user);

            return imageUrl;
        } catch (Exception e) {
            logger.error("프로필 이미지 업로드 실패: ", e);
            throw new ApiException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // User -> UserResponseDTO 변환 (profileImg 포함)
    private UserResponseDTO toResponse(User u) {
        return UserResponseDTO.builder()
                .userNo(u.getUserNo())
                .userId(u.getUserId())
                .nickname(u.getNickname())
                .email(u.getEmail())
                .phone(u.getPhone())
                .address(u.getAddress())
                .addressDetail(u.getAddressDetail())
                .userStatus(u.getUserStatus())
                .roleType(u.getRoleType())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .lastLogoutAt(u.getLastLogoutAt())
                .profileImg(u.getProfileImg())
                .build();
    }
}
