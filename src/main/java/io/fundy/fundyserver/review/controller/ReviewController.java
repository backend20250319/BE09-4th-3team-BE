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
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성 API
     * @param dto 리뷰 작성 요청 DTO
     * @param userId 인증된 사용자 ID
     * @return 생성된 리뷰 정보 (ReviewResponseDTO)와 HTTP 201 상태코드
     */
    @PostMapping("/create")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        ReviewResponseDTO response = reviewService.createReview(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 프로젝트에 대한 리뷰 목록 조회 (페이징, 정렬 포함)
     * @param projectNo 조회할 프로젝트 번호
     * @param page 요청할 페이지 번호 (기본값 0)
     * @param size 페이지당 조회 리뷰 개수 (기본값 5)
     * @param sort 정렬 기준 ("latest" 등, 기본값 "latest")
     * @return 페이징 처리된 리뷰 목록 (Page<ReviewResponseDTO>)
     */
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


    /**
     * 특정 프로젝트의 리뷰 미리보기(최근 5개 리뷰 조회)
     * @param projectNo 조회할 프로젝트 번호
     * @return 리뷰 목록 (최대 5개)
     */
    @GetMapping("/project/{projectNo}/preview")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewPreview(@PathVariable Long projectNo) {
        List<ReviewResponseDTO> preview = reviewService.getPreviewReviews(projectNo, 5);
        return ResponseEntity.ok(preview);
    }

    /**
     * 리뷰 수정 API
     * @param reviewNo 수정할 리뷰 번호
     * @param dto 리뷰 수정 요청 DTO
     * @param userId 인증된 사용자 ID (수정 권한 확인용)
     * @return 수정 결과 DTO (성공 여부 등 정보)
     */
    @PutMapping("/{reviewNo}")
    public ResponseEntity<ReviewUpdateResultDTO> updateReview(
            @PathVariable Long reviewNo,
            @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        ReviewUpdateResultDTO result = reviewService.updateReview(reviewNo, dto, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 리뷰 삭제 API
     * @param reviewNo 삭제할 리뷰 번호
     * @param userId 인증된 사용자 ID (삭제 권한 확인용)
     * @return 삭제 성공 메시지
     */
    @DeleteMapping("/{reviewNo}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewNo,
            @AuthenticationPrincipal String userId
    ) {
        reviewService.deleteReview(reviewNo, userId);
        return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 삭제되었습니다."));
    }


    /**
     * 현재 인증된 사용자가 리뷰 작성 가능한 프로젝트 목록 조회
     * (예: 참여했지만 아직 리뷰를 작성하지 않은 프로젝트)
     * @param userId 인증된 사용자 ID
     * @return 리뷰 작성 가능한 프로젝트 리스트
     */
    @GetMapping("/writable")
    public ResponseEntity<List<ReviewWritableProjectDTO>> getWritableProjects(
            @AuthenticationPrincipal String userId
    ) {
        List<ReviewWritableProjectDTO> result = reviewService.getWritableProjects(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 현재 인증된 사용자가 작성한 모든 리뷰 목록 조회
     * @param userId 인증된 사용자 ID
     * @return 사용자가 작성한 리뷰 리스트
     */
    @GetMapping("/written")
    public ResponseEntity<List<ReviewResponseDTO>> getWrittenReviews(
            @AuthenticationPrincipal String userId
    ) {
        List<ReviewResponseDTO> result = reviewService.getWrittenReviews(userId);
        return ResponseEntity.ok(result);
    }
}