package io.fundy.fundyserver.register.repository;

import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 로그인 ID로 조회
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);

    // 이메일로 조회 및 중복 검사
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 닉네임 중복 검사
    Optional<User> findByNickname(String nickname);
    boolean existsByNickname(String nickname);

    // 전화번호 중복 검사
    boolean existsByPhone(String phone);

    // 로그인 시 BAN 상태 사용자는 제외하고 조회
    Optional<User> findByUserIdAndUserStatusNot(String userId, UserStatus banned);

    // ADMIN에서 사용
    Page<User> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);
    Page<User> findByRoleTypeAndNicknameContainingIgnoreCase(RoleType roleType, String nickname, Pageable pageable);
    Page<User> findByRoleType(RoleType roleType, Pageable pageable);
}
