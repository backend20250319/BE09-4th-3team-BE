package io.fundy.fundyserver.project.repository;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 🔹 프로젝트 저장 (save는 JpaRepository에 있지만 명시적으로 재정의한 것)
    Project save(Project project);

    // 🔹 특정 카테고리의 프로젝트 목록 조회 (관리자 전용 페이지 등에서 사용)
    Page<Project> findByCategory_CategoryNo(Long categoryNo, Pageable pageable);

    // 🔹 특정 상태의 전체 프로젝트 수 카운트 (ex. APPROVED 몇 개 있는지)
    int countByProductStatus(ProjectStatus status);

    // 🔹 여러 프로젝트 번호로 한 번에 가져오기 (예: 즐겨찾기한 프로젝트 리스트 조회)
    List<Project> findAllByProjectNoIn(Collection<Long> projectNos);

    // ✅ 🔹 [목록용] 마감일이 지나지 않은 상태의 프로젝트 목록 조회 (APPROVED, IN_PROGRESS 등)
    Page<Project> findByProductStatusInAndDeadLineGreaterThanEqual(
            List<ProjectStatus> statuses, LocalDate today, Pageable pageable
    );

    // ✅ 🔹 [상태 자동전환용] APPROVED 상태이면서 시작일이 오늘이거나 이전인 프로젝트 조회 → IN_PROGRESS로 바꾸기 위함
    List<Project> findByProductStatusAndStartLineLessThanEqual(ProjectStatus status, LocalDate date);

    // ✅ 🔹 [목록용] 마감일이 지나지 않은 프로젝트 개수 조회 (APPROVED + IN_PROGRESS 전용)
    long countByProductStatusInAndDeadLineGreaterThanEqual(List<ProjectStatus> statuses, LocalDate today);

    // ✅ 🔹 [상태 자동전환용] 마감일이 지난 프로젝트 중에서 COMPLETED/FAILED 상태로 바꿔야 할 대상 조회
    List<Project> findByProductStatusInAndDeadLineBefore(List<ProjectStatus> statuses, LocalDate date);

    // 🔹 프로젝트 + 연관 카테고리 한 번에 조회 (N+1 문제 해결을 위한 EntityGraph 설정)
    @EntityGraph(attributePaths = "category")
    Page<Project> findAll(Pageable pageable);
}
