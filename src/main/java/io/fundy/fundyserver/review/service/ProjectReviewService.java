package io.fundy.fundyserver.review.service;

import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.entity.ProjectReview;
import io.fundy.fundyserver.review.repository.ProjectReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectReviewService {


    private final ProjectReviewRepository projectReviewRepository;
    private final UserRepository userRepository;

    // 리뷰 등록
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto, User user) {
        ProjectReview review = ProjectReview.createReview(
                dto.getProjectNo(),
                user,
                dto.getRewardStatus(),
                dto.getPlanStatus(),
                dto.getCommStatus(),
                dto.getContent(),
                null // 이미지 없으면 null
        );
        ProjectReview savedReview = projectReviewRepository.save(review);
        return toDTO(savedReview);
    }

    // 프로젝트 번호로 리뷰 목록 조회
    public List<ReviewResponseDTO> getReviewsByProjectNo(Long projectNo) {
        List<ProjectReview> reviews = projectReviewRepository.findByProjectNo(projectNo);
        return reviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO toDTO(ProjectReview review) {
        return new ReviewResponseDTO(
                review.getReviewNo(),
                review.getProjectNo(),
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