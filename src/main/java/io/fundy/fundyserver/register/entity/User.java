package io.fundy.fundyserver.register.entity;

import io.fundy.fundyserver.register.entity.RoleType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 사용자 정보를 저장하는 JPA 엔티티.
@Entity
@Table(name = "users")  // JPA 엔티티 선언
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // PK: 자동 증가 정수 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 자동 생성
    private Integer userNo;

    // 로그인 ID (유니크)
    @Column(name = "userId", length = 50, nullable = false, unique = true)
    private String userId;

    // 이메일 (유니크)
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    // 암호화된 비밀번호
    @Column(length = 255, nullable = false)
    private String password;

    // 사용자 닉네임 (유니크)
    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    // 전화번호
    @Column(length = 20, nullable = false, unique = true)
    private String phone;

    // 주소 (선택 입력)
    @Column(length = 200)
    private String address;

    // 상세 주소
    @Column(name = "address_detail")
    private String addressDetail;

    // 로그인/로그아웃/BANNED 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    // 유저 역할(RoleType): USER or ADMIN
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    // 가입 시각 (수정 불가)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 마지막 로그인 시간
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 마지막 로그아웃 시간
    @Column(name = "last_logout_at")
    private LocalDateTime lastLogoutAt;

    // 최초 저장 전 자동 실행
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // 수정 시 자동 실행
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 프로필 이미지 URL (nullable)
//    @Column(name = "profile_img", length = 255)
//    private String profileImg;
}
