package io.fundy.fundyserver.pledge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import io.fundy.fundyserver.project.dto.project.ProjectDetailResponseDTO;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyPledgeResponseDTO {
    private Long pledgeNo;
    private ProjectDetailResponseDTO project;
    private List<PledgeRewardInfoDTO> rewards; // 선택한 리워드 목록
    private Integer additionalAmount; // 추가 후원금
    private Integer totalAmount;
    private String deliveryAddress;
    private String deliveryPhone;
    private String recipientName;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PledgeRewardInfoDTO {
        private Long rewardNo; // 리워드 번호
        private String rewardTitle; // 리워드 제목
        private Integer rewardAmount; // 리워드 금액 (후원 시점)
        private Integer quantity; // 수량
    }
}
