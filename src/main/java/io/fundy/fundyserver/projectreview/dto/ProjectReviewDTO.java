package io.fundy.fundyserver.projectreview.dto;

import java.time.LocalDateTime;

public class ProjectReviewDTO {

    private Long reviewId;
    private Long projectId;
    private Long userId;
    private String nickname; // User 닉네임 (조인해서 세팅)
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
