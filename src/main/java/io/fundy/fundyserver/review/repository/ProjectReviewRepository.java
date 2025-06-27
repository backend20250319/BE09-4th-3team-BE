package io.fundy.fundyserver.review.repository;

import io.fundy.fundyserver.review.entity.ProjectReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectReviewRepository extends JpaRepository<ProjectReview, Long> {

//    List<ProjectReview> findByProject_ProjectId(Long projectId);
    List<ProjectReview> findByProjectNo(Long projectNo);
    List<ProjectReview> findByUser_Id(Long userId);

}
