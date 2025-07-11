package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.DashboardStatsDto;
import io.fundy.fundyserver.admin.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 1. 대시보드 통계 정보
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // 2. 프로젝트 상태별 개수
    @GetMapping("/project-status")
    public ResponseEntity<Map<String, Integer>> getProjectStatusCount() {
        Map<String, Integer> statusCounts = dashboardService.getProjectStatusCounts();
        return ResponseEntity.ok(statusCounts);
    }
}
