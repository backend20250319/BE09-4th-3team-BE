package io.fundy.fundyserver.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long reviewNo;
    private Long projectNo;
    private String projectTitle;
    private String userNickname;
    private int rewardStatus;
    private int planStatus;
    private int commStatus;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
