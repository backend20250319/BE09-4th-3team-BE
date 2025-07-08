package io.fundy.fundyserver.review.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ReviewWritableProjectDTO {
    private Long projectNo;
    private String title;
    private String imageUrl;
    private String rewardName;
    private String priceText;
    private LocalDate endDate;
}