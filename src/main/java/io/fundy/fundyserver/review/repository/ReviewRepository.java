package io.fundy.fundyserver.review.repository;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProject(Project project, Pageable pageable);

    Page<Review> findByProject_ProjectNo(Long projectNo, Pageable pageable);

    boolean existsByUserAndProject(User user, Project project);


    List<Review> findByUser_UserId(String userId);

    List<Review> findByUser_UserIdOrderByCreatedAtDesc(String userId);

}