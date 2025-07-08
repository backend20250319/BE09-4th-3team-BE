package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminUserResponseDto;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import io.fundy.fundyserver.register.entity.UserStatus;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * 사용자 목록을 닉네임 필터 및 페이징과 함께 조회
     *
     * @param page 요청 페이지 (0부터 시작)
     * @param nickname (선택) 닉네임 검색 키워드
     * @return AdminUserResponseDto 리스트를 포함한 페이지 객체
     */

    public Page<AdminUserResponseDto> getUserList(int page, String nickname) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;

        if (nickname != null && !nickname.trim().isEmpty()) {
            users = userRepository.findByRoleTypeAndNicknameContainingIgnoreCase(
                    RoleType.USER, nickname.trim(), pageable);
        } else {
            users = userRepository.findByRoleType(RoleType.USER, pageable);
        }

        return users.map(AdminUserResponseDto::fromEntity);
    }

    @Transactional
    public void updateUserStatus(Integer userNo, String statusKeyword) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 문자열로 들어온 상태값 처리
        String normalized = statusKeyword.trim().toUpperCase();

        // "BAN" → "BANNED"로 매핑
        if (normalized.equals("BAN")) {
            user.setUserStatus(UserStatus.BANNED);
        } else if (normalized.equals("UNBAN")) {
            user.setUserStatus(UserStatus.LOGOUT);  // 또는 LOGIN 으로도 가능
        } else {
            try {
                user.setUserStatus(UserStatus.valueOf(normalized));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("올바르지 않은 상태값입니다.");
            }
        }

        userRepository.save(user); // 변경사항 저장
    }
}


