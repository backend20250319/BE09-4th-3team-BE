package io.fundy.fundyserver.review.dto;//package io.fundy.fundyserver.projectreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {
    private Long projectId;
    private int rewardSatisfaction;
    private int planningSatisfaction;
    private int communicationSatisfaction;
    private String content;
    private MultipartFile image;
}