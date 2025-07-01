package io.fundy.fundyserver.register.repository;

import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthUserRepository extends JpaRepository<OAuthUser, Long> {
    Optional<OAuthUser> findByEmail(String email);
}
