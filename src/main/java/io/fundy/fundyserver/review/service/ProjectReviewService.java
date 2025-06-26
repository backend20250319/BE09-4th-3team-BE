package io.fundy.fundyserver.review.service;

import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.entity.ProjectReview;
import io.fundy.fundyserver.review.repository.ProjectReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectReviewService {

    private final ProjectReviewRepository projectReviewRepository; // repository 추가 필요

    public ReviewResponseDTO createReview(ReviewRequestDTO dto, User user) {

        ProjectReview review = ProjectReview.builder()
                .projectId(dto.getProjectId())
                .user(user)
                .rewardSatisfaction(dto.getRewardSatisfaction())
                .planningSatisfaction(dto.getPlanningSatisfaction())
                .communicationSatisfaction(dto.getCommunicationSatisfaction())
                .content(dto.getContent())
                .imageUrl(null)
                .build();

        ProjectReview savedReview = projectReviewRepository.save(review);

        return toDTO(savedReview);
    }

    private ReviewResponseDTO toDTO(ProjectReview review) {
        return new ReviewResponseDTO(
                review.getReviewId(),
                review.getProjectId(),
                review.getUser() != null ? review.getUser().getNickname() : null,
                review.getRewardSatisfaction(),
                review.getPlanningSatisfaction(),
                review.getCommunicationSatisfaction(),
                review.getContent(),
                review.getImageUrl(),
                review.getCreatedAt()
        );
    }
}