package io.fundy.fundyserver.pledge.repository;

import io.fundy.fundyserver.pledge.entity.Pledge;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
// AdminPledgeService에서 추가
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;

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

    @Query("SELECT DISTINCT p.user.userId FROM Pledge p WHERE p.project.projectNo = :projectNo")
    List<String> findDistinctUserIdsByProjectNo(@Param("projectNo") Long projectNo);

    // AdminPledgeService에서 추가
    @Query("SELECT p FROM Pledge p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.project prj")
    List<Pledge> findAllWithAssociations(); // 기존 (전체 조회)

    @Query(
            value = "SELECT p FROM Pledge p JOIN FETCH p.user u JOIN FETCH p.project prj",
            countQuery = "SELECT COUNT(p) FROM Pledge p"
    )
    Page<Pledge> findAllWithAssociations(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pledge p")
    Long countTotalPledges();

    @Query("SELECT SUM(p.totalAmount) FROM Pledge p")
    Long sumTotalPledgedAmount();

    @Query("SELECT COUNT(p) FROM Pledge p WHERE DATE(p.createdAt) = CURRENT_DATE")
    Long countTodayPledges();

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM Pledge p")
    Long countDistinctBackers();
}