package io.fundy.fundyserver.pledge.entity;

import io.fundy.fundyserver.project.entity.Reward;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pledge_rewards")
@Getter
@NoArgsConstructor
public class PledgeReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pledgeRewardNo; // 후원 리워드 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pledge_no", nullable = false)
    private Pledge pledge; // 소속 후원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_no", nullable = false)
    private Reward reward; // 선택한 리워드

    @Column(name = "reward_amount", nullable = false)
    private Integer rewardAmount; // 후원 시점의 리워드 금액

    @Column(name = "reward_title", nullable = false)
    private String rewardTitle; // 후원 시점의 리워드 타이틀

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1; // 리워드 수량

    // 연관관계 설정 메서드
    public void setPledge(Pledge pledge) {
        this.pledge = pledge;
    }

    public static PledgeReward create(Pledge pledge, Reward reward, Integer quantity) {
        PledgeReward pledgeReward = new PledgeReward();
        pledgeReward.pledge = pledge;
        pledgeReward.reward = reward;
        pledgeReward.rewardAmount = reward.getAmount();
        pledgeReward.rewardTitle = reward.getTitle();
        pledgeReward.quantity = quantity != null ? quantity : 1;
        return pledgeReward;
    }
}
