package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminReviewResponseDto;
import io.fundy.fundyserver.review.entity.ProjectReview;
import io.fundy.fundyserver.review.repository.ProjectReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ProjectReviewRepository reviewRepository;

    public List<AdminReviewResponseDto> getAllReviews(int page) {
        // page는 0부터 시작하므로 주의 (page=0이면 첫 페이지)
        PageRequest pageable = PageRequest.of(page, 10);
        Page<ProjectReview> reviewPage = reviewRepository.findAll(pageable);

        return reviewPage.stream()
                .map(review -> AdminReviewResponseDto.builder()
                        .reviewNo(review.getReviewNo())
                        .projectTitle(review.getProject().getTitle())
                        .userNickname(review.getUser().getNickname())
                        .rewardStatus(review.getRewardStatus())
                        .planStatus(review.getPlanStatus())
                        .commStatus(review.getCommStatus())
                        .content(review.getContent())
                        .imageUrl(review.getImageUrl())
                        .createdAt(review.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }
}