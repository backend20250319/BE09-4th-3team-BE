package io.fundy.fundyserver.review.entity;

import io.fundy.fundyserver.register.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "project_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_no")
    private Long reviewNo;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "project_id", nullable = false)
//    private Project project;

    @Column(name = "project_no", nullable = false)
    private Long projectNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
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

    public static ProjectReview createReview(Long projectNo, User user, int rewardStatus, int planStatus, int commStatus, String content, String imageUrl) {
        ProjectReview review = new ProjectReview();
        review.projectNo = projectNo;
        review.user = user;
        review.rewardStatus = rewardStatus;
        review.planStatus = planStatus;
        review.commStatus = commStatus;
        review.content = content;
        review.imageUrl = imageUrl;
        return review;
    }
}
