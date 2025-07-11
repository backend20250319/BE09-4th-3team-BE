package io.fundy.fundyserver.review.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewWritableProjectDTO {
    private Long projectNo;
    private String title;
    private String thumbnailUrl;
    private String rewardSummary;
    private Integer totalAmount;
    private LocalDate deadLine;
    private LocalDate pledgedAt;
}