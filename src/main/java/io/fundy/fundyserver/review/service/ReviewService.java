package io.fundy.fundyserver.review.service;

import io.fundy.fundyserver.pledge.dto.MyPledgeResponseDTO;
import io.fundy.fundyserver.pledge.service.PledgeService;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.dto.ReviewUpdateResultDTO;
import io.fundy.fundyserver.review.dto.ReviewWritableProjectDTO;
import io.fundy.fundyserver.review.entity.Review;
import io.fundy.fundyserver.review.exception.ReviewErrorCode;
import io.fundy.fundyserver.review.exception.ReviewException;
import io.fundy.fundyserver.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PledgeService pledgeService;

    private User findUserOrThrow(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.USER_NOT_FOUND));

        if (user.getRoleType() == RoleType.ADMIN) {
            throw new ReviewException(ReviewErrorCode.NOT_ALLOWED_FOR_ADMIN);
        }

        return user;
    }

    // 후원 내역 기준으로 참여 여부 체크
    private void checkParticipation(String userId, Long projectNo) {
        List<MyPledgeResponseDTO> pledges = pledgeService.getMyPledges(userId);
        // ⭐ 이 부분을 추가하여 null 체크 및 안전한 초기화
        if (pledges == null) {
            // 로그를 추가하여 null이 반환되었음을 기록
            System.err.println("[ReviewService] checkParticipation: pledgeService.getMyPledges for userId " + userId + " returned null.");
            pledges = Collections.emptyList(); // null 대신 빈 리스트로 초기화하여 NPE 방지
        }

        boolean hasPledged = pledges.stream()
                .anyMatch(pledge ->pledge.getProject().getProjectNo().equals(projectNo));

        if (!hasPledged) {
            throw new ReviewException(ReviewErrorCode.USER_NOT_PARTICIPATED);
        }
    }

    private void checkReviewOwnership(Review review, String userId) {
        if (!review.getUser().getUserId().equals(userId)) {
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }
    }

    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto, String userId) {
        User user = findUserOrThrow(userId);

        Project project = projectRepository.findById(dto.getProjectNo())
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        checkParticipation(userId, dto.getProjectNo());

        boolean activeReviewExists = reviewRepository.existsByUserAndProject(user, project);
        if (activeReviewExists) {
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
        Review savedReview = reviewRepository.save(review);
        return toDTO(savedReview);
    }

    public Page<ReviewResponseDTO> getReviewsByProjectNo(Long projectNo, int page, int size, String sortBy) {
        Sort sort = "satisfaction".equals(sortBy)
                ? Sort.by(Sort.Direction.DESC, "planStatus")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewPage = reviewRepository.findByProject_ProjectNo(projectNo, pageable);

        return reviewPage.map(this::toDTO);
    }

    public List<ReviewResponseDTO> getPreviewReviews(Long projectNo, int limit) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> page = reviewRepository.findByProject(project, pageable);

        return page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewUpdateResultDTO updateReview(Long reviewNo, ReviewRequestDTO dto, String userId) {
        findUserOrThrow(userId);

        Review review = reviewRepository.findById(reviewNo)
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

    @Transactional
    public void deleteReview(Long reviewNo, String userId) {

        userRepository.findByUserId(userId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        reviewRepository.delete(review);
    }

    // 후원했지만 아직 리뷰 안 쓴, 성공 마감된 프로젝트 목록 조회
    public List<ReviewWritableProjectDTO> getWritableProjects(String userId) {
        final List<MyPledgeResponseDTO> pledges = getPledgesSafely(userId);
        if (pledges.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> pledgedProjectNos = pledges.stream()
                .map(pledge -> pledge.getProject().getProjectNo())
                .collect(Collectors.toSet());

        List<Project> pledgedProjects = projectRepository.findAllByProjectNoIn(pledgedProjectNos);

        // 리뷰를 이미 작성한 프로젝트를 필터링하기 위한 map
        Map<Long, Boolean> hasActiveReviewMap = reviewRepository.findByUser_UserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getProject().getProjectNo(),
                        r -> true,
                        (a, b) -> a
                ));

        // DTO로 변환
        return pledgedProjects.stream()
                .filter(p -> p.getCurrentAmount() >= p.getGoalAmount())
                .filter(p -> p.getDeadLine() != null && p.getDeadLine().isBefore(LocalDate.now()))
                .filter(p -> !hasActiveReviewMap.containsKey(p.getProjectNo()))
                .map(p -> pledges.stream()
                        .filter(pl -> pl.getProject() != null && pl.getProject().getProjectNo().equals(p.getProjectNo()))
                        .findFirst()
                        .map(pl -> {
                            String rewardTitle = pl.getRewards() != null && !pl.getRewards().isEmpty()
                                    ? pl.getRewards().get(0).getRewardTitle()
                                    : null;

                            return new ReviewWritableProjectDTO(
                                    p.getProjectNo(),
                                    p.getTitle(),
                                    p.getThumbnailUrl(),
                                    rewardTitle,
                                    pl.getTotalAmount(),
                                    p.getDeadLine(),
                                    pl.getCreatedAt()
                            );
                        })
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .toList();
    }

    private List<MyPledgeResponseDTO> getPledgesSafely(String userId) {
        try {
            List<MyPledgeResponseDTO> pledges = pledgeService.getMyPledges(userId);
            return pledges != null ? pledges : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<ReviewResponseDTO> getWrittenReviews(String userId) {
        findUserOrThrow(userId);

        List<Review> writtenReviews = reviewRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        return writtenReviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO toDTO(Review review) {
        Project project = review.getProject();

        return new ReviewResponseDTO(
                review.getReviewNo(),
                project != null ? project.getProjectNo() : null,
                project != null ? project.getTitle() : null,
                review.getUser() != null ? review.getUser().getNickname() : null,
                review.getRewardStatus(),
                review.getPlanStatus(),
                review.getCommStatus(),
                review.getContent(),
                project != null ? project.getThumbnailUrl() : null,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}