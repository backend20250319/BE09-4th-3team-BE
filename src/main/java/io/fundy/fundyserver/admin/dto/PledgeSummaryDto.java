package io.fundy.fundyserver.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PledgeSummaryDto {
    private Long totalPledgeCount;     // 전체 후원 건수
    private Long totalPledgedAmount;   // 전체 후원 금액
    private Long todayPledgeCount;     // 오늘 후원 건수
    private Long totalBackerCount;     // 전체 후원자 수
}
