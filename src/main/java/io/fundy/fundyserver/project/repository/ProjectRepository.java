package io.fundy.fundyserver.project.repository;

import io.fundy.fundyserver.project.entity.Project;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository {
    Project save(Project project);
}
