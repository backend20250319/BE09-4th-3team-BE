package io.fundy.fundyserver.project.repository;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project save(Project project);

    // Admin project 추가 부분
    Page<Project> findByCategory_CategoryNo(Long categoryNo, Pageable pageable);

    /* admin에서 전체 프로젝트 수 세기 위해 추가*/
    long countByProductStatus(ProjectStatus projectStatus);

    @EntityGraph(attributePaths = "category")
    Page<Project> findAll(Pageable pageable);
}
