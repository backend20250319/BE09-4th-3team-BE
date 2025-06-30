package io.fundy.fundyserver.review.service;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.entity.ProjectReview;
import io.fundy.fundyserver.review.exception.ReviewErrorCode;
import io.fundy.fundyserver.review.exception.ReviewException;
import io.fundy.fundyserver.review.repository.ParticipationRepository;
import io.fundy.fundyserver.review.repository.ProjectReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ProjectReviewService {

    private final ProjectReviewRepository projectReviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ParticipationRepository participationRepository;

    // 리뷰 등록
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto, Integer userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findById(dto.getProjectNo())
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        // ✅ (1) 관리자 후기 작성 차단
        if ("ADMIN".equals(user.getRoleType())) {
            throw new ReviewException(ReviewErrorCode.NOT_ALLOWED_FOR_ADMIN);
        }

        // ✅ (2) 프로젝트 참여자만 작성 가능
        boolean isParticipant = participationRepository.existsByUser_UserNoAndProject_ProjectNo(userNo, dto.getProjectNo());
        if (!isParticipant) {
            throw new ReviewException(ReviewErrorCode.USER_NOT_PARTICIPATED);
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

    public List<ReviewResponseDTO> getReviewsByProjectNo(Long projectNo) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PROJECT_NOT_FOUND));

        List<ProjectReview> reviews = projectReviewRepository.findByProject(project);

        return reviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 엔티티 -> 응답 DTO
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
                review.getCreatedAt()
        );
    }
}