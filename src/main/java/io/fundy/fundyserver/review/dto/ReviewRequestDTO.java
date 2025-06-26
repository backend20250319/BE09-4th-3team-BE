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
    private Long projectNo;
    private int rewardStatus;
    private int planStatus;
    private int commStatus;
    private String content;
    private MultipartFile image;
}