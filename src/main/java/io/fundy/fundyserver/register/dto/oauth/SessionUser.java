package io.fundy.fundyserver.register.dto.oauth;

import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.entity.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private final Long id;
    private final String name;
    private final String email;
    private final String picture;
    private final String nickname; // 닉네임 필드 추가
    private final String registrationId;
    private final String userType; // OAUTH 또는 NORMAL

    // OAuthUser 전용 생성자
    public SessionUser(OAuthUser user) {
        this.id = user.getOauthId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.nickname = user.getNickname(); // 닉네임 설정
        this.registrationId = user.getRegistrationId();
        this.userType = "OAUTH";
    }

    // 일반 회원 전용 생성자
    public SessionUser(User user) {
        this.id = Long.valueOf(user.getUserNo());
        this.name = user.getNickname();
        this.email = user.getEmail();
        this.picture = null;
        this.nickname = user.getNickname(); // 닉네임 설정
        this.registrationId = "NORMAL";
        this.userType = "NORMAL";
    }

    // 직접 필드 지정
    public SessionUser(Long id, String name, String email, String picture, String nickname, String registrationId, String userType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.nickname = nickname;
        this.registrationId = registrationId;
        this.userType = userType;
    }
}