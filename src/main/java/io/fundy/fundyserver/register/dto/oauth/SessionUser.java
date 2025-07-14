package io.fundy.fundyserver.register.dto.oauth;

import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.entity.RoleType; // 추가
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private final Long id;
    private final String name;
    private final String email;
    private final String picture;
    private final String nickname;
    private final String registrationId;
    private final String userType; // OAUTH 또는 NORMAL
    private final String roleType; // ✅ 역할: USER 또는 ADMIN

    // OAuthUser 전용 생성자
    public SessionUser(OAuthUser user) {
        this.id = user.getOauthId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.nickname = user.getNickname();
        this.registrationId = user.getRegistrationId();
        this.userType = "OAUTH";
        this.roleType = user.getRoleType().name(); // ✅ enum → string
    }

    // 일반 회원 전용 생성자
    public SessionUser(User user) {
        this.id = Long.valueOf(user.getUserNo());
        this.name = user.getNickname();
        this.email = user.getEmail();
        this.picture = null;
        this.nickname = user.getNickname();
        this.registrationId = "NORMAL";
        this.userType = "NORMAL";
        this.roleType = user.getRoleType().name(); // ✅ enum → string
    }

    // 커스텀 생성자
    public SessionUser(Long id, String name, String email, String picture,
                       String nickname, String registrationId, String userType, String roleType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.nickname = nickname;
        this.registrationId = registrationId;
        this.userType = userType;
        this.roleType = roleType;
    }
}