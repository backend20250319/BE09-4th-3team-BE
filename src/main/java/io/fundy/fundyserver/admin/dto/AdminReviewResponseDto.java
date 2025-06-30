package io.fundy.fundyserver.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReviewResponseDto {

    private Long reviewNo;
    private String projectTitle;
    private String userNickname;
    private int rewardStatus;
    private int planStatus;
    private int commStatus;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
}

