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

    // 프로필 수정을 위한 추가 필드들
    @Column(name = "nickname")
    private String nickname;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

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

    // 프로필 수정을 위한 Setter 메서드들
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    // Builder 패턴을 위한 추가 메서드들
    public OAuthUser withNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public OAuthUser withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public OAuthUser withAddress(String address) {
        this.address = address;
        return this;
    }

    public OAuthUser withAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
        return this;
    }
}