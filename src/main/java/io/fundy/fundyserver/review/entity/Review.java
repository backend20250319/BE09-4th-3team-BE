package io.fundy.fundyserver.review.entity;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_no")
    private Long reviewNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_no", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reward_status", nullable = false)
    private int rewardStatus;

    @Column(name = "plan_status", nullable = false)
    private int planStatus;

    @Column(name = "comm_status", nullable = false)
    private int commStatus;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Review createReview(Project project, User user, int rewardStatus, int planStatus, int commStatus, String content, String imageUrl) {
        Review review = new Review();
        review.project = project;
        review.user = user;
        review.rewardStatus = rewardStatus;
        review.planStatus = planStatus;
        review.commStatus = commStatus;
        review.content = content;
        review.imageUrl = imageUrl;
        return review;
    }

    public void updateReview(int rewardStatus, int planStatus, int commStatus, String content, String imageUrl) {
        this.rewardStatus = rewardStatus;
        this.planStatus = planStatus;
        this.commStatus = commStatus;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // 리뷰 재활성화 메서드 추가
    public void reactivateReview(Integer rewardStatus, Integer planStatus, Integer commStatus, String content, String imageUrl) {
        this.rewardStatus = rewardStatus;
        this.planStatus = planStatus;
        this.commStatus = commStatus;
        this.content = content;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }
}