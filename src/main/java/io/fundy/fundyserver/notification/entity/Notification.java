package io.fundy.fundyserver.notification.entity;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_no")
    private Long notificationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_no", nullable = false)
    private Project project;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at", nullable =        false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @PrePersist
    public void prePersist() {
        if (isRead == null) {
            isRead = false;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}