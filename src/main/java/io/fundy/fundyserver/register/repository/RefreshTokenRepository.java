package io.fundy.fundyserver.register.repository;

import io.fundy.fundyserver.register.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
}