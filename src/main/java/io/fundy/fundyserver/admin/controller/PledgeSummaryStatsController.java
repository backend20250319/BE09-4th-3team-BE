package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.PledgeSummaryStatsDto;
import io.fundy.fundyserver.admin.service.PledgeSummaryStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/pledge-stats")
@RequiredArgsConstructor
public class PledgeSummaryStatsController {

    private final PledgeSummaryStatsService pledgeSummaryStatsService;

    /**
     * 관리자 통계용 후원 요약 정보 조회
     */
    @GetMapping("/summary")
    public PledgeSummaryStatsDto getPledgeSummaryStats() {
        return pledgeSummaryStatsService.getStats();
    }
}