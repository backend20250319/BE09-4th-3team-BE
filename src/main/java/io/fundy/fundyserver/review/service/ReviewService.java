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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PledgeService pledgeService;


    /**
     * 사용자 아이디로 사용자 조회, 없으면 예외 발생
     * @param userId 조회할 사용자 ID
     * @return User 엔티티
     */
    private User findUserOrThrow(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.USER_NOT_FOUND));

        return user;
    }

    /**
     * 사용자 후원 내역으로 프로젝트 참여 여부 체크
     * 참여하지 않았으면 예외 발생
     * @param userId 사용자 ID
     * @param projectNo 프로젝트 번호
     */
    private void checkParticipation(String userId, Long projectNo) {
        List<MyPledgeResponseDTO> pledges = pledgeService.getMyPledges(userId);

        // null 반환 시 빈 리스트로 초기화하여 NPE 방지
        if (pledges == null) {
            System.err.println("[ReviewService] checkParticipation: pledgeService.getMyPledges for userId " + userId + " returned null.");
            pledges = Collections.emptyList();
        }

        boolean hasPledged = pledges.stream()
                .anyMatch(pledge ->pledge.getProject().getProjectNo().equals(projectNo));

        if (!hasPledged) {
            throw new ReviewException(ReviewErrorCode.USER_NOT_PARTICIPATED);
        }
    }


    /**
     * 리뷰 소유자 권한 확인
     * 소유자가 아니면 예외 발생
     * @param review 리뷰 엔티티
     * @param userId 사용자 ID
     */
    private void checkReviewOwnership(Review review, String userId) {
        if (!review.getUser().getUserId().equals(userId)) {
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }
    }

    /**
     * 리뷰 생성
     * 참여여부 및 중복 리뷰 작성 여부 체크 후 저장
     * @param dto 리뷰 요청 DTO
     * @param userId 작성자 사용자 ID
     * @return 저장된 리뷰 DTO
     */
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


    /**
     * 프로젝트 번호로 리뷰 조회 (페이징 + 정렬)
     * @param projectNo 프로젝트 번호
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param sortBy 정렬 기준 ("satisfaction" 시 planStatus 기준 내림차순, 기본은 createdAt 내림차순)
     * @return 페이징된 리뷰 DTO 페이지
     */
    public Page<ReviewResponseDTO> getReviewsByProjectNo(Long projectNo, int page, int size, String sortBy) {
        Sort sort = "satisfaction".equals(sortBy)
                ? Sort.by(Sort.Direction.DESC, "planStatus")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewPage = reviewRepository.findByProject_ProjectNo(projectNo, pageable);

        return reviewPage.map(this::toDTO);
    }

    /**
     * 프로젝트의 최근 리뷰 일부만 미리보기용으로 조회
     * @param projectNo 프로젝트 번호
     * @param limit 조회할 리뷰 최대 개수
     * @return 리뷰 DTO 리스트
     */
    public List<ReviewResponseDTO> getPreviewReviews(Long projectNo, int limit) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> page = reviewRepository.findByProject(project, pageable);

        return page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 리뷰 수정
     * 소유자 확인, 참여 여부 확인 후 리뷰 내용 업데이트
     * @param reviewNo 수정할 리뷰 번호
     * @param dto 리뷰 수정 요청 DTO
     * @param userId 요청자 사용자 ID
     * @return 수정 전후 리뷰 DTO를 담은 결과 객체
     */
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

    /**
     * 리뷰 삭제
     * 사용자 존재 및 리뷰 소유권 확인 후 삭제
     * @param reviewNo 삭제할 리뷰 번호
     * @param userId 요청자 사용자 ID
     */
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

    /**
     * 후원했으나 아직 리뷰를 작성하지 않은, 성공적으로 종료된 프로젝트 목록 조회
     * @param userId 사용자 ID
     * @return 리뷰 작성 가능한 프로젝트 DTO 리스트
     */
    public List<ReviewWritableProjectDTO> getWritableProjects(String userId) {
        final List<MyPledgeResponseDTO> pledges = getPledgesSafely(userId);



        for (MyPledgeResponseDTO pl : pledges) {
            System.out.println("Pledge projectNo: " + (pl.getProject() != null ? pl.getProject().getProjectNo() : "null"));
            System.out.println("Rewards:");
            if (pl.getRewards() != null) {
                pl.getRewards().forEach(r -> System.out.println(" - " + r.getRewardTitle() + " x " + r.getQuantity()));
            } else {
                System.out.println(" - No rewards");
            }
        }

        if (pledges.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> pledgedProjectNos = pledges.stream()
                .map(pledge -> pledge.getProject().getProjectNo())
                .collect(Collectors.toSet());

        List<Project> pledgedProjects = projectRepository.findAllByProjectNoIn(pledgedProjectNos);

        Map<Long, Boolean> hasActiveReviewMap = reviewRepository.findByUser_UserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getProject().getProjectNo(),
                        r -> true,
                        (a, b) -> a
                ));

        // 성공 마감되고 리뷰 안 쓴 프로젝트 필터링 후 DTO 매핑
        return pledgedProjects.stream()
                .filter(p -> p.getCurrentAmount() >= p.getGoalAmount())
                .filter(p -> p.getDeadLine() != null && p.getDeadLine().isBefore(LocalDate.now()))
                .filter(p -> !hasActiveReviewMap.containsKey(p.getProjectNo()))
                .map(p -> {
                    // 이 프로젝트에 해당하는 모든 후원 내역 리스트
                    List<MyPledgeResponseDTO> projectPledges = pledges.stream()
                            .filter(pl -> pl.getProject() != null && pl.getProject().getProjectNo().equals(p.getProjectNo()))
                            .toList();

                    // 모든 후원 리워드 타이틀+수량을 하나로 합침
                    String rewardSummary = projectPledges.stream()
                            .flatMap(pl -> pl.getRewards() != null
                                    ? pl.getRewards().stream()
                                    : Stream.<MyPledgeResponseDTO.PledgeRewardInfoDTO>empty())
                            .map(r -> r.getRewardTitle() + " x " + r.getQuantity())
                            .collect(Collectors.joining("\n"));

                    // 첫 번째 후원 날짜 (대표 날짜)
                    LocalDate pledgedDate = null;
                    if (!projectPledges.isEmpty() && projectPledges.get(0).getCreatedAt() != null) {
                        pledgedDate = projectPledges.get(0).getCreatedAt().toLocalDate();
                    }

                    // 총 후원 금액도 여러 후원 합산 가능하면 바꿔야 함
                    Integer totalAmount = projectPledges.stream()
                            .map(MyPledgeResponseDTO::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(0, Integer::sum);

                    return new ReviewWritableProjectDTO(
                            p.getProjectNo(),
                            p.getTitle(),
                            p.getThumbnailUrl(),
                            p.getCreatorName(),  // String
                            projectPledges.stream()
                                    .flatMap(pl -> pl.getRewards() != null ? pl.getRewards().stream() : Stream.empty())
                                    .collect(Collectors.toList()),  // List<PledgeRewardInfoDTO>
                            totalAmount,
                            p.getDeadLine(),
                            pledgedDate,
                            rewardSummary   // String
                    );
                })
                .toList();
    }

    /**
     * 후원 내역을 안전하게 가져오는 메서드
     * 예외나 null 발생 시 빈 리스트 반환
     * @param userId 사용자 ID
     * @return 후원 내역 리스트 또는 빈 리스트
     */
    private List<MyPledgeResponseDTO> getPledgesSafely(String userId) {
        try {
            List<MyPledgeResponseDTO> pledges = pledgeService.getMyPledges(userId);
            return pledges != null ? pledges : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 사용자가 작성한 모든 리뷰 조회 (최신순)
     * @param userId 사용자 ID
     * @return 리뷰 DTO 리스트
     */
    public List<ReviewResponseDTO> getWrittenReviews(String userId) {
        findUserOrThrow(userId);

        List<Review> writtenReviews = reviewRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        return writtenReviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Review 엔티티를 ReviewResponseDTO로 변환
     * @param review Review 엔티티
     * @return ReviewResponseDTO 객체
     */
    private ReviewResponseDTO toDTO(Review review) {
        Project project = review.getProject();

        return new ReviewResponseDTO(
                review.getReviewNo(),
                project != null ? project.getProjectNo() : null,
                review.getUser() != null ? review.getUser().getUserId() : null,
                project != null ? project.getTitle() : null,
                project != null ? project.getCreatorName() : null,
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