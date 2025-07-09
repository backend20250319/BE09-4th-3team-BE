package io.fundy.fundyserver.pledge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PledgeResponseDTO {
    private Long pledgeNo; // 생성된 후원 ID
    private Long projectNo; // 후원한 프로젝트 번호
    private String projectTitle; // 프로젝트 제목
    private String rewardTitle; // 선택한 리워드 제목
    private Integer totalAmount; // 총 후원 금액
}
