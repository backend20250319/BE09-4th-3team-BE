package io.fundy.fundyserver.register.security;

import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomOAuthUserDetails implements UserDetails {

    private final OAuthUser oauthUser;

    public CustomOAuthUserDetails(OAuthUser oauthUser) {
        this.oauthUser = oauthUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> oauthUser.getRole().getKey()); // ex) "ROLE_USER"
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인은 패스워드 사용하지 않음
    }

    @Override
    public String getUsername() {
        return oauthUser.getEmail(); // 이메일이 고유 사용자 식별자
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 기본 설정
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 기본 설정
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 기본 설정
    }

    @Override
    public boolean isEnabled() {
        return true; // 기본 설정
    }

    public OAuthUser getOAuthUser() {
        return this.oauthUser; // ❗ 누락된 return 문 추가
    }
}
