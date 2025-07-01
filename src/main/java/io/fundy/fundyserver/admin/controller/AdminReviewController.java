package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminReviewResponseDto;
import io.fundy.fundyserver.admin.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<List<AdminReviewResponseDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page // 기본값은 0페이지
    ) {
        return ResponseEntity.ok(adminReviewService.getAllReviews(page));
    }
}
