package io.fundy.fundyserver.review.repository;

import io.fundy.fundyserver.review.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    boolean existsByUser_UserNoAndProject_ProjectNo(Integer userNo, Long projectNo);
}