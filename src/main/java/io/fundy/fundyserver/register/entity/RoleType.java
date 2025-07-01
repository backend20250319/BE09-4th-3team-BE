package io.fundy.fundyserver.register.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 사용자 역할(RoleType) 타입
// USER  : 일반 사용자
// ADMIN : 관리자
@Getter
@RequiredArgsConstructor
public enum RoleType {
    USER("ROLE_USER"),
    GUEST("ROLE_GUEST"),  // 👈 소셜 로그인 임시 사용자용 추가
    ADMIN("ROLE_ADMIN");  // ✅ UserRole 대체

    private final String key;
}
