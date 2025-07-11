package io.fundy.fundyserver.review.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReviewWritableProjectDTO {
    private Long projectNo;
    private String projectTitle;
    private String projectThumbnailUrl;
    private String rewardTitle;
    private Integer rewardAmount;
    private LocalDate deadLine;
    private LocalDateTime pledgeCreatedAt;
}