package io.fundy.fundyserver.review.controller;

import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.dto.ReviewUpdateResultDTO;
import io.fundy.fundyserver.review.dto.ReviewWritableProjectDTO;
import io.fundy.fundyserver.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        ReviewResponseDTO response = reviewService.createReview(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/project/{projectNo}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviews(
            @PathVariable Long projectNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByProjectNo(projectNo, page, size, sort);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/project/{projectNo}/preview")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewPreview(@PathVariable Long projectNo) {
        List<ReviewResponseDTO> preview = reviewService.getPreviewReviews(projectNo, 5);
        return ResponseEntity.ok(preview);
    }

    @PutMapping("/{reviewNo}")
    public ResponseEntity<ReviewUpdateResultDTO> updateReview(
            @PathVariable Long reviewNo,
            @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        ReviewUpdateResultDTO result = reviewService.updateReview(reviewNo, dto, userId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reviewNo}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewNo,
            @AuthenticationPrincipal String userId
    ) {
        reviewService.deleteReview(reviewNo, userId);
        return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 삭제되었습니다."));
    }

    @GetMapping("/writable")
    public ResponseEntity<List<ReviewWritableProjectDTO>> getWritableProjects(
            @AuthenticationPrincipal String userId
    ) {
        System.out.println("=== REVIEW API 호출 시작 ===");
        System.out.println("userId: " + userId);
        System.out.println("현재 시간: " + LocalDateTime.now());

        try {
            if (userId == null) {
                System.out.println("ERROR: userId가 null입니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<ReviewWritableProjectDTO> result = reviewService.getWritableProjects(userId);
            System.out.println("성공: 결과 개수 = " + result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("=== 에러 발생 ===");
            System.err.println("에러 타입: " + e.getClass().getSimpleName());
            System.err.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    // 삭제된 리뷰가 있는 프로젝트 목록 조회 API 제거 (deleted 필드 없어서 의미 없음)

    @GetMapping("/written")
    public ResponseEntity<List<ReviewResponseDTO>> getWrittenReviews(
            @AuthenticationPrincipal String userId
    ) {
        List<ReviewResponseDTO> result = reviewService.getWrittenReviews(userId);
        return ResponseEntity.ok(result);
    }
}