package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminReviewResponseDto;
import io.fundy.fundyserver.review.entity.Review;
import io.fundy.fundyserver.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * 페이지네이션이 적용된 리뷰 목록 반환
     *
     * @param page 페이지 번호 (0부터 시작)
     * @return Page<AdminReviewResponseDto> 페이징된 리뷰 목록
     */
    public Page<AdminReviewResponseDto> getAllReviews(int page) {
        PageRequest pageable = PageRequest.of(page, 10); // 한 페이지에 10개
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        return reviewPage.map(review -> AdminReviewResponseDto.builder()
                .reviewNo(review.getReviewNo())
                .projectTitle(review.getProject().getTitle())
                .userNickname(review.getUser().getNickname())
                .rewardStatus(review.getRewardStatus())
                .planStatus(review.getPlanStatus())
                .commStatus(review.getCommStatus())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build()
        );
    }
}