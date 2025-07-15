package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminPledgesResponseDto;
import io.fundy.fundyserver.admin.dto.DailyFundingDto;
import io.fundy.fundyserver.admin.service.AdminPledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/pledges")
public class AdminPledgeController {

    private final AdminPledgeService adminPledgeService;

    @GetMapping
    public ResponseEntity<List<AdminPledgesResponseDto>> getAll() {
        return ResponseEntity.ok(adminPledgeService.getAllPledges());
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<DailyFundingDto>> getDailyFundingSummary() {
        List<DailyFundingDto> summary = adminPledgeService.getDailyFundingSummary();
        return ResponseEntity.ok(summary);
    }
}
