package io.fundy.fundyserver.admin.dto;

import io.fundy.fundyserver.register.entity.User;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponseDto {

    private Integer userNo;            // 사용자 고유 ID (PK)
    private String userId;             // 로그인 ID
    private String email;              // 이메일
    private String nickname;           // 닉네임
    private String phone;              // 전화번호
    private String address;            // 주소
    private String accountNumber;      // 계좌번호
    private String userStatus;         // LOGIN / LOGOUT / BANNED
    private String roleType;           // USER / ADMIN
    private String createdAt;          // 가입일
    private String updatedAt;          // 수정일

    // Entity -> DTO 변환
    public static AdminUserResponseDto fromEntity(User user) {
        return AdminUserResponseDto.builder()
                .userNo(user.getUserNo())
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .userStatus(user.getUserStatus().name())       // Enum -> String
                .roleType(user.getRoleType().name())           // Enum -> String
                .createdAt(formatDate(user.getCreatedAt()))    // LocalDateTime -> String
                .updatedAt(formatDate(user.getUpdatedAt()))
                .build();
    }

    private static String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
