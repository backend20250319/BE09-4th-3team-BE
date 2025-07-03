package io.fundy.fundyserver.notification.repository;

import io.fundy.fundyserver.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_UserNo(Integer userNo);

    List<Notification> findByUser_UserNoAndType(Integer userNo, String type);
}