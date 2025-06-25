package io.fundy.fundyserver.register.security;

import io.fundy.fundyserver.register.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRoleType().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserId(); // 로그인 ID 기준
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 추후 만료 처리 필요 시 커스터마이징
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getUserStatus().name().equals("BANNED");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // 탈퇴 회원 구분 로직 넣을 수 있음
    }
}
