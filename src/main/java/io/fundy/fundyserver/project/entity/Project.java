package io.fundy.fundyserver.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 프로젝트 고유 ID

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user; // 등록한 창작자 (회원)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 카테고리

    @Column(nullable = false, length = 100)
    private String title; // 프로젝트 제목

    @Lob
    @Column(nullable = false)
    private String description; // 상세 설명

    @Column(name = "goal_amount", nullable = false)
    private Integer goalAmount; // 목표 금액

    @Column(name = "current_amount", nullable = false)
    private Integer currentAmount = 0; // 현재 후원 금액

    @Column(nullable = false)
    private LocalDate deadline; // 마감일

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false, length = 30)
    private ProjectStatus productStatus = ProjectStatus.WAITING_APPROVAL; // 프로젝트 상태

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl; // 썸네일 이미지 URL

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0; // 조회수

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정일

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reward> rewards = new ArrayList<>();

    public void addReward(Reward reward) {
        rewards.add(reward);
        reward.setProject(this);
    }
}


