package io.fundy.fundyserver.review.service;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.dto.ReviewUpdateResultDTO;
import io.fundy.fundyserver.review.dto.ReviewWritableProjectDTO;
import io.fundy.fundyserver.review.entity.Participation;
import io.fundy.fundyserver.review.entity.Review;
import io.fundy.fundyserver.review.exception.ReviewErrorCode;
import io.fundy.fundyserver.review.exception.ReviewException;
import io.fundy.fundyserver.review.repository.ParticipationRepository;
import io.fundy.fundyserver.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectReviewService {

    private final ReviewRepository projectReviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ParticipationRepository participationRepository;

    // 사용자 조회 + 관리자 권한 체크 + 존재 여부 확인
    private User findUserOrThrow(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.USER_NOT_FOUND));

        if (user.getRoleType() == RoleType.ADMIN) {
            throw new ReviewException(ReviewErrorCode.NOT_ALLOWED_FOR_ADMIN);
        }

        return user;
    }

    // 프로젝트 참여 여부 확인
    private void checkParticipation(String userId, Long projectNo) {
        boolean isParticipant = participationRepository.existsByUser_UserIdAndProject_ProjectNo(userId, projectNo);
        if (!isParticipant) {
            throw new ReviewException(ReviewErrorCode.USER_NOT_PARTICIPATED);
        }
    }

    // 리뷰 작성자 권한 확인
    private void checkReviewOwnership(Review review, String userId) {
        if (!review.getUser().getUserId().equals(userId)) {
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }
    }

    // 후기 등록
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto, String userId) {
        User user = findUserOrThrow(userId);

        Project project = projectRepository.findById(dto.getProjectNo())
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        checkParticipation(userId, dto.getProjectNo());

        boolean exists = projectReviewRepository.existsByUserAndProject(user, project);
        if (exists) {
            throw new ReviewException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.createReview(
                project,
                user,
                dto.getRewardStatus().getValue(),
                dto.getPlanStatus().getValue(),
                dto.getCommStatus().getValue(),
                dto.getContent(),
                null
        );

        Review savedReview = projectReviewRepository.save(review);
        return toDTO(savedReview);
    }

    // 전체 후기 - 페이징 조회
    public Page<ReviewResponseDTO> getReviewsByProjectNo(Long projectNo, int page, int size, String sortBy) {
        Sort sort;
        if ("satisfaction".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "planStatus"); // 만족도순 정렬 처리
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순 정렬 처리
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewPage = projectReviewRepository.findByProject_ProjectNo(projectNo, pageable);

        return reviewPage.map(this::toDTO);
    }

    // 미리보기 - 최신순 상위 N개만 조회
    public List<ReviewResponseDTO> getPreviewReviews(Long projectNo, int limit) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> page = projectReviewRepository.findByProject(project, pageable);

        return page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    @Transactional
    public ReviewUpdateResultDTO updateReview(Long reviewNo, ReviewRequestDTO dto, String userId) {
        findUserOrThrow(userId);

        Review review = projectReviewRepository.findById(reviewNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        checkReviewOwnership(review, userId);
        checkParticipation(userId, dto.getProjectNo());

        ReviewResponseDTO beforeUpdate = toDTO(review);

        review.updateReview(
                dto.getRewardStatus().getValue(),
                dto.getPlanStatus().getValue(),
                dto.getCommStatus().getValue(),
                dto.getContent(),
                null
        );

        ReviewResponseDTO afterUpdate = toDTO(review);

        return new ReviewUpdateResultDTO(beforeUpdate, afterUpdate);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewNo, String userId) {
        findUserOrThrow(userId);

        Review review = projectReviewRepository.findById(reviewNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        checkReviewOwnership(review, userId);
        checkParticipation(userId, review.getProject().getProjectNo());

        projectReviewRepository.delete(review);
    }

    // 작성 가능한 프로젝트 목록 조회
    public List<ReviewWritableProjectDTO> getWritableProjects(String userId) {
        // 1. 유저가 참여한 프로젝트들 가져오기
        List<Project> participatedProjects = participationRepository.findByUser_UserId(userId)
                .stream()
                .map(Participation::getProject) // 메서드 참조로 변경
                .toList();

        // 2. 유저가 이미 작성한 리뷰들 가져오기
        List<Long> reviewedProjectNos = projectReviewRepository.findByUser_UserId(userId)
                .stream()
                .map(r -> r.getProject().getProjectNo())
                .toList();

        // 3. 조건에 맞는 프로젝트만 필터링
        return participatedProjects.stream()
                .filter(p -> p.getCurrentAmount() >= p.getGoalAmount()) // 목표금액 달성
                .filter(p -> p.getDeadLine().isBefore(LocalDate.now())) // 마감됨
                .filter(p -> !reviewedProjectNos.contains(p.getProjectNo())) // 리뷰 아직 안 씀
                .map(p -> new ReviewWritableProjectDTO(
                        p.getProjectNo(),
                        p.getTitle(),
                        p.getThumbnailUrl(),   // imageUrl → thumbnailUrl로 변경
                        "",                    // rewardName → 비워둠 or rewards.get(0) 등으로 채울 수 있음
                        "",                    // priceText → 없으면 빈 문자열
                        p.getDeadLine()
                ))
                .toList();
    }

    // 사용자가 작성한 후기 목록 조회
    public List<ReviewResponseDTO> getWrittenReviews(String userId) {
        // 존재 확인
        findUserOrThrow(userId);

        List<Review> writtenReviews = projectReviewRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        return writtenReviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO toDTO(Review review) {
        return new ReviewResponseDTO(
                review.getReviewNo(),
                review.getProject() != null ? review.getProject().getProjectNo() : null,
                review.getUser() != null ? review.getUser().getNickname() : null,
                review.getRewardStatus(),
                review.getPlanStatus(),
                review.getCommStatus(),
                review.getContent(),
                review.getImageUrl(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}