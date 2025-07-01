package io.fundy.fundyserver.register.repository;

import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String>  {

    Optional<RefreshToken> findByToken(String token);     // ✅ 토큰 값으로 조회
    Optional<RefreshToken> findByUser(User user);         // ✅ User 기준 조회
    void deleteByUser(User user);                         // ✅ User 기준 삭제
}
