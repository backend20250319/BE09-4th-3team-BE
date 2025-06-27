//package io.fundy.fundyserver.review.controller;
//
//import io.fundy.fundyserver.register.entity.User;
//import io.fundy.fundyserver.register.security.CustomUserDetails;
//import io.fundy.fundyserver.review.dto.ReviewRequestDTO;
//import io.fundy.fundyserver.review.dto.ReviewResponseDTO;
//import io.fundy.fundyserver.review.service.ProjectReviewService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/reviews")
//@RequiredArgsConstructor
//public class ProjectReviewController {
//
//    private final ProjectReviewService reviewService;
//
//    @PostMapping
//    public ResponseEntity<?> createReview(@RequestBody ReviewRequestDTO dto,
//                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
//        User user = userDetails.getUser();
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(reviewService.createReview(dto, user));
//    }
//
//    // 특정 프로젝트 리뷰 리스트 조회
//    @GetMapping("/project/{projectNo}")
//    public ResponseEntity<List<ReviewResponseDTO>> getReviews(@PathVariable Long projectNo) {
//        List<ReviewResponseDTO> reviews = reviewService.getReviewsByProjectNo(projectNo);
//        return ResponseEntity.ok(reviews);
//    }
//}