package io.fundy.fundyserver.notification.repository;

import io.fundy.fundyserver.notification.entity.Notification;
import io.fundy.fundyserver.register.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_UserId(String userId, Pageable pageable);
    Page<Notification> findByUser_UserIdAndType(String userId, String type, Pageable pageable);
    long countByUser_UserIdAndIsReadFalse(String userId);
    List<Notification> findByUser_UserIdAndIsReadFalse(String userId);
}