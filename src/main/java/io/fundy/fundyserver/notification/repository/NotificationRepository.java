package io.fundy.fundyserver.notification.repository;

import io.fundy.fundyserver.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 삭제되지 않은 특정 사용자의 알림을 페이징 조회
     * @param userId 사용자 ID
     * @param pageable 페이징 및 정렬 정보
     * @return 삭제되지 않은 알림 페이징 결과
     */
    Page<Notification> findByUser_UserIdAndIsDeletedFalse(String userId, Pageable pageable);

    /**
     * 삭제되지 않은 특정 사용자의 특정 타입 알림을 페이징 조회
     * @param userId 사용자 ID
     * @param type 알림 타입 (예: "support", "success" 등)
     * @param pageable 페이징 및 정렬 정보
     * @return 삭제되지 않은 해당 타입 알림 페이징 결과
     */
    Page<Notification> findByUser_UserIdAndTypeAndIsDeletedFalse(String userId, String type, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은(안 읽은) 알림 개수 조회 (삭제되지 않은 것만)
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    long countByUser_UserIdAndIsReadFalseAndIsDeletedFalse(String userId);

    /**
     * 특정 사용자의 읽지 않은(안 읽은) 삭제되지 않은 알림 목록 조회
     * @param userId 사용자 ID
     * @return 읽지 않은 삭제되지 않은 알림 리스트
     */
    List<Notification> findByUser_UserIdAndIsReadFalseAndIsDeletedFalse(String userId);

    // 중복 체크, 프로젝트별 한 번의 후원만 가능할 경우
//    boolean existsByUser_UserIdAndProject_ProjectNoAndTypeAndMessage(
//            String userId, Long projectNo, String type, String message);

}