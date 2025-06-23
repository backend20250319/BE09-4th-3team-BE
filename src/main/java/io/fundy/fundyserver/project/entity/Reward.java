package io.fundy.fundyserver.project.entity;

import io.fundy.fundyserver.project.dto.reward.RewardRequestDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rewards")
@Getter
@NoArgsConstructor
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 리워드 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 소속 프로젝트

    @Column(nullable = false, length = 100)
    private String title; // 리워드 제목

    @Column(nullable = false)
    private Integer amount; // 후원 금액 기준

    @Lob
    @Column(nullable = false)
    private String description; // 리워드 상세 설명

    @Column
    private Integer stock; // 수량 제한 (-1 또는 null 가능)

    public void setProject(Project project) {
        this.project = project;
        // 필요 시 양방향 연관관계 유지
        if (project != null && !project.getRewards().contains(this)) {
            project.getRewards().add(this);
        }
    }

    public static Reward of(RewardRequestDTO dto, Project project) {
        Reward reward = new Reward();
        reward.title = dto.getTitle();
        reward.amount = dto.getAmount();
        reward.description = dto.getDescription();
        reward.stock = dto.getStock();
        reward.setProject(project); // 연관관계 설정
        return reward;
    }

}

