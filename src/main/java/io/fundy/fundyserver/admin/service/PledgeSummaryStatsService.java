package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.PledgeSummaryStatsDto;
import io.fundy.fundyserver.pledge.repository.PledgeRewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PledgeSummaryStatsService {

    private final PledgeRewardRepository pledgeRewardRepository;

    public PledgeSummaryStatsDto getStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Long totalPledges = pledgeRewardRepository.countTotalPledges();
        Long totalAmount = pledgeRewardRepository.sumTotalPledgedAmount();
        Long newPledgesToday = pledgeRewardRepository.countTodayPledges(startOfToday);
        Long totalBackers = pledgeRewardRepository.countDistinctBackers();

        return new PledgeSummaryStatsDto(
                totalPledges != null ? totalPledges : 0L,
                totalAmount != null ? totalAmount : 0L,
                newPledgesToday != null ? newPledgesToday : 0L,
                totalBackers != null ? totalBackers : 0L
        );
    }
}
