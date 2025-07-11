package io.fundy.fundyserver.review.repository;

import io.fundy.fundyserver.review.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    boolean existsByUser_UserIdAndProject_ProjectNo(String userId, Long projectNo);
    List<Participation> findByUser_UserId(String userId);
}