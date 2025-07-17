package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminPledgesResponseDto;
import io.fundy.fundyserver.pledge.entity.Pledge;
import io.fundy.fundyserver.pledge.repository.PledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import io.fundy.fundyserver.admin.dto.DailyFundingDto;
import java.util.Map;
import java.util.Comparator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class AdminPledgeService {

    private final PledgeRepository pledgeRepository;

    /**
     * 전체 후원 내역 조회 (비페이징)
     */
    public List<AdminPledgesResponseDto> getAllPledges() {
        List<Pledge> pledges = pledgeRepository.findAllWithAssociations(); // 커스텀 조인 fetch 필요
        return pledges.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 엔티티 → DTO 변환 메서드
     */
    private AdminPledgesResponseDto convertToDto(Pledge pledge) {
        return AdminPledgesResponseDto.builder()
                .pledgeNo(pledge.getPledgeNo())
                .userName(pledge.getUser().getNickname())
                .userEmail(pledge.getUser().getEmail())
                .projectTitle(pledge.getProject().getTitle())
                .totalAmount(pledge.getTotalAmount())
                .additionalAmount(pledge.getAdditionalAmount())
                .recipientName(pledge.getRecipientName())
                .deliveryAddress(pledge.getDeliveryAddress())
                .deliveryPhone(pledge.getDeliveryPhone())
                .createdAt(pledge.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }

    public List<DailyFundingDto> getDailyFundingSummary() {
        List<Pledge> pledges = pledgeRepository.findAllWithAssociations();

        // 그룹핑: 날짜별 총 금액
        Map<String, Integer> grouped = pledges.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCreatedAt().toLocalDate().toString(), // "yyyy-MM-dd"
                        Collectors.summingInt(Pledge::getTotalAmount)
                ));

        // DTO로 변환 및 정렬
        return grouped.entrySet().stream()
                .map(e -> new DailyFundingDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DailyFundingDto::getDate))
                .collect(Collectors.toList());
    }
    public Page<AdminPledgesResponseDto> getPledgesWithPaging(Pageable pageable) {
        return pledgeRepository.findAllWithAssociations(pageable)
                .map(this::convertToDto);
    }
}

