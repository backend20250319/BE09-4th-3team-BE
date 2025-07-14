package io.fundy.fundyserver.review.dto;


import io.fundy.fundyserver.pledge.dto.MyPledgeResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewWritableProjectDTO {
    private Long projectNo;
    private String title;
    private String thumbnailUrl;
    private String creatorName;
    private List<MyPledgeResponseDTO.PledgeRewardInfoDTO> rewards;
    private Integer totalAmount;
    private LocalDate deadLine;
    private LocalDate pledgedAt;
    private String rewardSummary;
}