package io.fundy.fundyserver.pledge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PledgeResponseDTO {
    private Long pledgeNo; // 생성된 후원 ID
    private Long projectNo; // 후원한 프로젝트 번호
    private String projectTitle; // 프로젝트 제목
    private List<PledgeRewardInfoDTO> rewards; // 선택한 리워드 목록
    private Integer additionalAmount; // 추가 후원금
    private Integer totalAmount; // 총 후원 금액

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
