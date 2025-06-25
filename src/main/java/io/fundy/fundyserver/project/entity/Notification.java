//package io.fundy.fundyserver.notification.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.Id;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "notifications")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Notification {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long notificationId;
//
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "user_id", nullable = false)
////    private User user;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "project_id", nullable = false)
////    private Project project;
//
//    @Column(length = 50, nullable = false)
//    private String type;
//
//    @Column(columnDefinition = "TEXT", nullable = false)
//    private String message;
//
//    @Column(nullable = false)
//    private Boolean isRead = false;
//
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//    }
//}
