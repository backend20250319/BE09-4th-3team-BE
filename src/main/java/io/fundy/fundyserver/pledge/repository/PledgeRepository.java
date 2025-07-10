package io.fundy.fundyserver.pledge.repository;

import io.fundy.fundyserver.pledge.entity.Pledge;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface PledgeRepository extends JpaRepository<Pledge, Long> {
    List<Pledge> findByUser(User user);
    List<Pledge> findByProject(Project project);
    /**
     * 특정 프로젝트에서 주어진 시간 이전에 생성된 후원 수를 조회
     * @param project 프로젝트
     * @param createdAt 기준 시간
     * @return 이전에 생성된 후원 수
     */
    int countByProjectAndCreatedAtBefore(Project project, LocalDateTime createdAt);
}