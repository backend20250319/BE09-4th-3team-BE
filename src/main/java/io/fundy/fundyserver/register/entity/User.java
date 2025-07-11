package io.fundy.fundyserver.register.entity;

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
    //  PK: 자동 증가 정수 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 자동 생성
    private Integer id;

    //  로그인 ID (유니크)
    @Column(name = "userId", length = 50, nullable = false, unique = true)  // 컬럼 속성(길이, null 허용, unique) 지정
    private String userId;

    //  이메일 (유니크)
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    //  암호화된 비밀번호
    @Column(length = 255, nullable = false)
    private String password;

    // 사용자 닉네임 (유니크)
    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    // 전화번호
    @Column(length = 20, nullable = false)
    private String phone;

    // 주소 (선택 입력)
    @Column(length = 200)
    private String address;

    // 계좌번호 (선택 입력)
    @Column(name = "account_number", length = 30)
    private String accountNumber;

    // 로그인/로그아웃/BANNED 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    // 유저 역할(RoleType): USER or ADMIN
    @Enumerated(EnumType.STRING)        // Enum을 문자열로 저장
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    // 가입 시각 (수정 불가)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 최초 저장 전 자동 실행
    @PrePersist // 생명주기 콜백(@PrePersist, @PreUpdate)으로 생성/수정 시각 자동 관리
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    //수정 시 자동 실행
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}