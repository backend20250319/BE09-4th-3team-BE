package io.fundy.fundyserver.project.repository;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project save(Project project);

    // Admin project 추가 부분
    Page<Project> findByCategory_CategoryNo(Long categoryNo, Pageable pageable);

    /* admin에서 전체 프로젝트 수 세기 위해 추가*/
    int countByProductStatus(ProjectStatus status);

    List<Project> findAllByProjectNoIn(Collection<Long> projectNos);

    // 마감일 이후 데이터는 가져오지 않게 함
    Page<Project> findByProductStatusInAndDeadLineGreaterThanEqual(
            List<ProjectStatus> statuses, LocalDate today, Pageable pageable
    );

    List<Project> findByProductStatusAndStartLineLessThanEqual(ProjectStatus status, LocalDate date);

    long countByProductStatusInAndDeadLineGreaterThanEqual(List<ProjectStatus> statuses, LocalDate today);

    // 마감일이 지나지 않은 승인 처리 된 전체 프로젝트의 수
    long countByProductStatusAndDeadLineGreaterThanEqual(ProjectStatus status, LocalDate today);

    List<Project> findByProductStatusInAndDeadLineBefore(List<ProjectStatus> statuses, LocalDate date);

    @EntityGraph(attributePaths = "category")
    Page<Project> findAll(Pageable pageable);
}
