package io.fundy.fundyserver.review.service;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.dto.ReviewUpdateResultDTO;
import io.fundy.fundyserver.review.entity.ProjectReview;
import io.fundy.fundyserver.review.exception.ReviewErrorCode;
import io.fundy.fundyserver.review.exception.ReviewException;
import io.fundy.fundyserver.review.repository.ParticipationRepository;
import io.fundy.fundyserver.review.repository.ProjectReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectReviewService {

    private final ProjectReviewRepository projectReviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ParticipationRepository participationRepository;

    // 후기 등록
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto, Integer userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findById(dto.getProjectNo())
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        // 관리자 차단
        if (user.getRoleType() == RoleType.ADMIN) {
            throw new ReviewException(ReviewErrorCode.NOT_ALLOWED_FOR_ADMIN);
        }

        // 참여자만 작성 가능
        boolean isParticipant = participationRepository.existsByUser_UserNoAndProject_ProjectNo(userNo, dto.getProjectNo());
        if (!isParticipant) {
            throw new ReviewException(ReviewErrorCode.USER_NOT_PARTICIPATED);
        }

        // 중복 작성 방지
        boolean exists = projectReviewRepository.existsByUserAndProject(user, project);
        if (exists) {
            throw new ReviewException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        ProjectReview review = ProjectReview.createReview(
                project,
                user,
                dto.getRewardStatus().getValue(),
                dto.getPlanStatus().getValue(),
                dto.getCommStatus().getValue(),
                dto.getContent(),
                null
        );

        ProjectReview savedReview = projectReviewRepository.save(review);
        return toDTO(savedReview);
    }

    // 전체 후기 - 페이징 조회
    public Page<ReviewResponseDTO> getReviewsByProjectNo(Long projectNo, int page, int size) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectReview> reviewPage = projectReviewRepository.findByProject(project, pageable);

        return reviewPage.map(this::toDTO);
    }

    // 미리보기 - 최신순 상위 N개만 조회
    public List<ReviewResponseDTO> getPreviewReviews(Long projectNo, int limit) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectReview> page = projectReviewRepository.findByProject(project, pageable);

        return page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    @Transactional
    public ReviewUpdateResultDTO updateReview(Long reviewNo, ReviewRequestDTO dto, Integer userNo) {
        ProjectReview review = projectReviewRepository.findById(reviewNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserNo().equals(userNo)) {
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        boolean isParticipant = participationRepository.existsByUser_UserNoAndProject_ProjectNo(userNo, dto.getProjectNo());
        if (!isParticipant) {
            throw new ReviewException(ReviewErrorCode.USER_NOT_PARTICIPATED);
        }

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
    public void deleteReview(Long reviewNo, Integer userNo) {
        ProjectReview review = projectReviewRepository.findById(reviewNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 작성자 확인
        if (!review.getUser().getUserNo().equals(userNo)) {
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // 참여 여부 확인
        boolean isParticipant = participationRepository.existsByUser_UserNoAndProject_ProjectNo(userNo, review.getProject().getProjectNo());
        if (!isParticipant) {
            throw new ReviewException(ReviewErrorCode.USER_NOT_PARTICIPATED);
        }

        // 삭제
        projectReviewRepository.delete(review);
    }


    private ReviewResponseDTO toDTO(ProjectReview review) {
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