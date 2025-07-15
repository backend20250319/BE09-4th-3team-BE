package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminPledgesResponseDto;
import io.fundy.fundyserver.admin.dto.DailyFundingDto;
import io.fundy.fundyserver.admin.dto.PledgeSummaryDto;
import io.fundy.fundyserver.admin.service.AdminPledgeService;
import io.fundy.fundyserver.admin.service.PledgeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/pledges")
public class AdminPledgeController {

    private final AdminPledgeService adminPledgeService;
    private final PledgeSummaryService pledgeSummaryService;

    @GetMapping
    public ResponseEntity<Page<AdminPledgesResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(adminPledgeService.getPledgesWithPaging(pageable));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<DailyFundingDto>> getDailyFundingSummary() {
        List<DailyFundingDto> summary = adminPledgeService.getDailyFundingSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary")
    public ResponseEntity<PledgeSummaryDto> getPledgeSummary() {
        return ResponseEntity.ok(pledgeSummaryService.getPledgeSummary());
    }
}
