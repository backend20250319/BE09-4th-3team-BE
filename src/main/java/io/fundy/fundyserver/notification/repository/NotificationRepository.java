package io.fundy.fundyserver.notification.repository;

import io.fundy.fundyserver.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_UserNo(Integer userNo, Pageable pageable);
    Page<Notification> findByUser_UserNoAndType(Integer userNo, String type, Pageable pageable);
}