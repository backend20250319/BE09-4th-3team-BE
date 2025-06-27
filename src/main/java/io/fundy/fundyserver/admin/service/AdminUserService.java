package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminUserResponseDto;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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

}
