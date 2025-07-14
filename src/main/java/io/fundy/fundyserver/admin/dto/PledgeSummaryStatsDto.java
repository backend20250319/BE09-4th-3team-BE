package io.fundy.fundyserver.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PledgeSummaryStatsDto {

    private Long totalPledgeCount;     // 총 후원 건수 (pledge 기준)
    private Long totalPledgedAmount;   // 총 후원 금액 (금액 기준)
    private Long todayPledgeCount;     // 오늘 발생한 후원 건수
    private Long totalBackerCount;     // 총 후원자 수
}
