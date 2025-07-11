package io.fundy.fundyserver.notification.repository;

import io.fundy.fundyserver.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_UserIdAndIsDeletedFalse(String userId, Pageable pageable);

    Page<Notification> findByUser_UserIdAndTypeAndIsDeletedFalse(String userId, String type, Pageable pageable);

    long countByUser_UserIdAndIsReadFalseAndIsDeletedFalse(String userId);

    List<Notification> findByUser_UserIdAndIsReadFalseAndIsDeletedFalse(String userId);
}