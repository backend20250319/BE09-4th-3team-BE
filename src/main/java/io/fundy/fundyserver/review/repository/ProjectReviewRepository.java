package io.fundy.fundyserver.review.repository;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.review.entity.ProjectReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectReviewRepository extends JpaRepository<ProjectReview, Long> {

    Page<ProjectReview> findByProject(Project project, Pageable pageable);

    boolean existsByUserAndProject(User user, Project project);
    List<ProjectReview> findByUser_UserNo(Integer userNo);

    // AdminReviewService에서 추가
    Page<ProjectReview> findAll( Pageable pageable);
}
