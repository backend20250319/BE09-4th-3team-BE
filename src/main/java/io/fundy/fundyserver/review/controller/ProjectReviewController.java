package io.fundy.fundyserver.review.controller;

import io.fundy.fundyserver.register.security.CustomUserDetails;
import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.dto.ReviewUpdateResultDTO;
import io.fundy.fundyserver.review.service.ProjectReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ProjectReviewController {

    private final ProjectReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO dto,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userNo = userDetails.getUser().getUserNo();
        ReviewResponseDTO response = reviewService.createReview(dto, userNo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 전체 후기 조회
    @GetMapping("/project/{projectNo}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviews(
            @PathVariable Long projectNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByProjectNo(projectNo, page, size);
        return ResponseEntity.ok(reviews);
    }

    // 미리보기용: 최신 5개
    @GetMapping("/project/{projectNo}/preview")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewPreview(@PathVariable Long projectNo) {
        List<ReviewResponseDTO> preview = reviewService.getPreviewReviews(projectNo, 5);
        return ResponseEntity.ok(preview);
    }

    // 리뷰 수정
    @PutMapping("/{reviewNo}")
    public ResponseEntity<ReviewUpdateResultDTO> updateReview(
            @PathVariable Long reviewNo,
            @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Integer userNo = userDetails.getUser().getUserNo();
        ReviewUpdateResultDTO result = reviewService.updateReview(reviewNo, dto, userNo);
        return ResponseEntity.ok(result);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewNo}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Integer userNo = userDetails.getUser().getUserNo();
        reviewService.deleteReview(reviewNo, userNo);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

}