package io.fundy.fundyserver.pledge.repository;

import io.fundy.fundyserver.pledge.entity.Pledge;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PledgeRepository extends JpaRepository<Pledge, Long> {
    List<Pledge> findByUser(User user);
    List<Pledge> findByProject(Project project);
}
