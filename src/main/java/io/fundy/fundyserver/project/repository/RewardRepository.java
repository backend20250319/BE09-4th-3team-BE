package io.fundy.fundyserver.project.repository;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByProject(Project project);
}
