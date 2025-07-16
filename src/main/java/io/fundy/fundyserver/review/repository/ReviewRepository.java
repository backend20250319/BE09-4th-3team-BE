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

    /**
     * 특정 프로젝트에 속한 리뷰들을 페이징 처리하여 조회
     * @param project 조회할 프로젝트 엔티티
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 리뷰 목록
     */
    Page<Review> findByProject(Project project, Pageable pageable);

    /**
     * 프로젝트 번호로 리뷰들을 페이징 처리하여 조회
     * @param projectNo 조회할 프로젝트 번호
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 리뷰 목록
     */
    Page<Review> findByProject_ProjectNo(Long projectNo, Pageable pageable);

    /**
     * 특정 유저가 특정 프로젝트에 이미 리뷰를 작성했는지 여부 확인
     * @param user 리뷰 작성자 유저 엔티티
     * @param project 대상 프로젝트 엔티티
     * @return 존재 여부(true면 이미 작성됨)
     */
    boolean existsByUserAndProject(User user, Project project);

    /**
     * 특정 유저가 작성한 모든 리뷰 조회 (정렬 없이)
     * @param userId 조회할 유저 ID
     * @return 리뷰 리스트
     */
    List<Review> findByUser_UserId(String userId);

    /**
     * 특정 유저가 작성한 모든 리뷰를 작성일자 내림차순으로 정렬하여 조회
     * @param userId 조회할 유저 ID
     * @return 작성일자 최신순 정렬된 리뷰 리스트
     */
    List<Review> findByUser_UserIdOrderByCreatedAtDesc(String userId);

}