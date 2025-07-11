package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminPledgesRewardsResponseDto;
import io.fundy.fundyserver.admin.service.AdminPledgesRewardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/pledge-rewards")
@RequiredArgsConstructor
public class AdminPledgesRewardsController {

    private final AdminPledgesRewardsService adminPledgesRewardsService;

    /**
     * 관리자용 후원 리워드 목록 조회 (페이징 처리)
     * GET /admin/pledge-rewards?page=0&size=10
     */
    @GetMapping
    public Page<AdminPledgesRewardsResponseDto> getPledgeRewards(Pageable pageable) {
        return adminPledgesRewardsService.getPledgeRewards(pageable);
    }
}
