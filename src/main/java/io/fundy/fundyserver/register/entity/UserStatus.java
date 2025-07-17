package io.fundy.fundyserver.register.entity;


// 사용자 상태를 나타내는 Enum.
// LOGIN  : 로그인 상태
// LOGOUT : 로그아웃 상태
// BANNED : 정지된 상태
// QUIT : 탈퇴 처리

public enum UserStatus {
    LOGIN,
    LOGOUT,
    BANNED,
    QUIT
}