package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminPledgesRewardsResponseDto;
import io.fundy.fundyserver.pledge.entity.PledgeReward;
import io.fundy.fundyserver.pledge.repository.PledgeRewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AdminPledgesRewardsService {

    private final PledgeRewardRepository pledgeRewardRepository; // ✅ 올바른 리포지토리 주입

    public Page<AdminPledgesRewardsResponseDto> getPledgeRewards(Pageable pageable) {
        Page<PledgeReward> pledgeRewards = pledgeRewardRepository.findAllWithAssociations(pageable); // ✅ 수정됨
        return pledgeRewards.map(this::convertToDto);
    }

    private AdminPledgesRewardsResponseDto convertToDto(PledgeReward pr) {
        return AdminPledgesRewardsResponseDto.builder()
                .pledgeRewardNo(pr.getPledgeRewardNo())
                .pledgeNo(pr.getPledge().getPledgeNo())
                .userName(pr.getPledge().getUser().getNickname())
                .userEmail(pr.getPledge().getUser().getEmail())
                .projectNo(pr.getPledge().getProject().getProjectNo())
                .projectTitle(pr.getPledge().getProject().getTitle())
                .rewardNo(pr.getReward().getRewardNo())
                .rewardTitle(pr.getRewardTitle())
                .rewardAmount(pr.getRewardAmount())
                .quantity(pr.getQuantity())
                .totalRewardAmount(pr.getRewardAmount() * pr.getQuantity())
                .recipientName(pr.getPledge().getRecipientName())
                .deliveryAddress(pr.getPledge().getDeliveryAddress())
                .deliveryPhone(pr.getPledge().getDeliveryPhone())
                .createdAt(pr.getPledge().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }
}
