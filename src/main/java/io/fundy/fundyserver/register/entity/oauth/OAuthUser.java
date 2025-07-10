package io.fundy.fundyserver.register.entity.oauth;

import io.fundy.fundyserver.register.entity.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "oauth_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OAuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_id")
    private Long oauthId;

    private String name;

    private String email;

    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private RoleType roleType; // 명확하게 필드명 통일

    @Column(name = "registration_id")
    private String registrationId;

    // 이름, 프로필 이미지 업데이트
    public OAuthUser update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    // 역할 문자열 반환 (예: ROLE_USER)
    public String getUserRoleKey() {
        return this.roleType.getKey();
    }

    // OAuth2SuccessHandler 에서 사용
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    // 컴파일 오류 방지용 Getter
    public RoleType getRole() {
        return this.roleType;
    }
}
