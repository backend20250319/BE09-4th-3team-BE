package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.PledgeSummaryDto;
import io.fundy.fundyserver.pledge.repository.PledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PledgeSummaryService {

    // ✅ 의존성 주입 필드 추가
    private final PledgeRepository pledgeRepository;

    public PledgeSummaryDto getPledgeSummary() {
        // ✅ 인스턴스를 통해 메서드 호출
        Long totalCount = pledgeRepository.countTotalPledges();
        Long totalAmount = pledgeRepository.sumTotalPledgedAmount();
        Long todayCount = pledgeRepository.countTodayPledges();
        Long backerCount = pledgeRepository.countDistinctBackers();

        return new PledgeSummaryDto(
                totalCount != null ? totalCount : 0L,
                totalAmount != null ? totalAmount : 0L,
                todayCount != null ? todayCount : 0L,
                backerCount != null ? backerCount : 0L
        );
    }
}
