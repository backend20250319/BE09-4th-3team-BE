package io.fundy.fundyserver.review.controller;

import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
import io.fundy.fundyserver.review.dto.ReviewUpdateResultDTO;
import io.fundy.fundyserver.review.dto.ReviewWritableProjectDTO;
import io.fundy.fundyserver.review.service.ProjectReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ProjectReviewService reviewService;
    /**
     * 리뷰 작성
     * @param dto - 리뷰 작성에 필요한 데이터가 담긴 DTO
     * @param user - 인증된 사용자의 ID
     * @return 생성된 리뷰의 상세 정보를 담은 DTO
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal String user
    ) {

        ReviewResponseDTO response = reviewService.createReview(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 프로젝트에 대한 후기 목록 조회 (페이징 및 정렬 지원)
     */
    @GetMapping("/project/{projectNo}")
    public ResponseEntity<Page<?>> getReviews(
            @PathVariable Long projectNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Page<?> reviews = reviewService.getReviewsByProjectNo(projectNo, page, size, sort);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 특정 프로젝트의 최신 5개 리뷰 미리보기 조회
     */
    @GetMapping("/project/{projectNo}/preview")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewPreview(@PathVariable Long projectNo) {
        List<ReviewResponseDTO> preview = reviewService.getPreviewReviews(projectNo, 5);
        return ResponseEntity.ok(preview);
    }

    /**
     * 특정 리뷰 수정
     * @param user - 인증된 사용자 ID
     */
    @PutMapping("/{reviewNo}")
    public ResponseEntity<ReviewUpdateResultDTO> updateReview(
            @PathVariable Long reviewNo,
            @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal String user
    ) {
        System.out.println("인증된 사용자: " + user);
        ReviewUpdateResultDTO result = reviewService.updateReview(reviewNo, dto, user);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 리뷰 삭제
     */
    @DeleteMapping("/{reviewNo}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewNo,
            @AuthenticationPrincipal String user
    ) {
        reviewService.deleteReview(reviewNo, user);
        return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 삭제되었습니다."));
    }

    /**
     * 리뷰 작성 가능한 프로젝트 목록 조회
     */
    @GetMapping("/writable")
    public ResponseEntity<List<ReviewWritableProjectDTO>> getWritableProjects(
            @RequestParam String user
    ) {
        List<ReviewWritableProjectDTO> result = reviewService.getWritableProjects(user);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 사용자가 작성한 모든 리뷰 조회
     */
    @GetMapping("/written")
    public ResponseEntity<List<ReviewResponseDTO>> getWrittenReviews(
            @RequestParam String user
    ) {
        List<ReviewResponseDTO> result = reviewService.getWrittenReviews(user);
        return ResponseEntity.ok(result);
    }
}