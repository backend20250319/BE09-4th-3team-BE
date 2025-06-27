package io.fundy.fundyserver.admin.dto;

import io.fundy.fundyserver.register.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponseDto {

    private Integer no; // 조회 순번 또는 row index
    private String userId;         // 로그인 ID
    private String email;          // 이메일 주소
    private String nickname;       // 사용자 이름
    private String phone;          // 전화번호
    private String address;        // 주소
    private String accountNumber;  // 계좌번호
    private String userStatus;     // LOGIN, LOGOUT, BANNED
    private String roleType;       // USER, ADMIN
    private String createdAt;      // 가입일
    private String updatedAt;      // 수정일

    public static AdminUserResponseDto fromEntity(User user) {
        return AdminUserResponseDto.builder()
                .no(user.getUserNo())
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .userStatus(user.getUserStatus().name())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}

