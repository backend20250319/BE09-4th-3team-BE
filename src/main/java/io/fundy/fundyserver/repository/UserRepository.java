package io.fundy.fundyserver.repository;

import io.fundy.fundyserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
