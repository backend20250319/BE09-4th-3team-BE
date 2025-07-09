//package io.fundy.fundyserver.notification.controller;
//
//
//import io.fundy.fundyserver.notification.dto.NotificationRequestDTO;
//import io.fundy.fundyserver.notification.dto.NotificationResponseDTO;
//import io.fundy.fundyserver.notification.dto.NotificationSendRequestDTO;
//import io.fundy.fundyserver.notification.service.NotificationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/notifications")
//@RequiredArgsConstructor
//public class NotificationController {
//
//    private final NotificationService notificationService;
//
//
//    /**
//     * 후원 완료 알림 전송
//     * @param dto - userNo, projectNo, projectTitle 포함한 전송용 DTO
//     * @return 성공 메시지
//     */
//    @PostMapping("/support")
//    public ResponseEntity<String> sendSupport(@RequestBody NotificationSendRequestDTO dto) {
//        notificationService.sendSupportComplete(dto.getUserId(), dto.getProjectNo(), dto.getProjectTitle());
//        return ResponseEntity.ok("후원 완료 알림이 성공적으로 전송되었습니다.");
//    }
//
//    /**
//     * 프로젝트 성공 마감 알림 전송
//     * @param dto - userNo, projectNo, projectTitle 포함한 전송용 DTO
//     * @return 성공 메시지 또는 에러 상태 및 메시지 반환
//     */
//    @PostMapping("/success")
//    public ResponseEntity<String> sendSuccess(@RequestBody NotificationSendRequestDTO dto) {
//        try {
//            notificationService.sendProjectSuccess(dto.getUserId(), dto.getProjectNo(), dto.getProjectTitle());
//            return ResponseEntity.ok("프로젝트 성공 알림이 성공적으로 전송되었습니다.");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (IllegalStateException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }
//
//    /**
//     * 프로젝트 실패 마감 알림 전송
//     * @param dto - userNo, projectNo, projectTitle 포함한 전송용 DTO
//     * @return 성공 메시지 또는 에러 상태 및 메시지 반환
//     */
//    @PostMapping("/fail")
//    public ResponseEntity<String> sendFail(@RequestBody NotificationSendRequestDTO dto) {
//        try {
//            notificationService.sendProjectFail(dto.getUserId(), dto.getProjectNo(), dto.getProjectTitle());
//            return ResponseEntity.ok("프로젝트 실패 알림이 성공적으로 전송되었습니다.");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (IllegalStateException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/{notificationNo}")
//    public ResponseEntity<Void> deleteNotification(
//            @PathVariable Long notificationNo,
//            @RequestParam String userId
//    ) {
//        notificationService.deleteNotification(notificationNo, userId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/unread-count")
//    public ResponseEntity<Long> getUnreadCount(@RequestParam String userId) {
//        long count = notificationService.countUnreadNotifications(userId);
//        return ResponseEntity.ok(count);
//    }
//
//    /**
//     * 특정 사용자의 모든 알림을 읽음 처리
//     * @param userId 사용자 번호
//     * @return 성공 메시지
//     */
//    @PatchMapping("/mark-all-read")
//    public ResponseEntity<String> markAllAsRead(@RequestParam String userId) {
//        notificationService.markAllNotificationsAsRead(userId);
//        return ResponseEntity.ok("모든 알림이 읽음 처리되었습니다.");
//    }
//
//    /**
//     * 알림 목록 조회 (페이징, 필터링 가능)
//     * @param dto - userNo, type(알림 유형), page, size 포함한 조회용 DTO
//     * @return 페이징된 알림 리스트
//     */
//    @GetMapping
//    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(NotificationRequestDTO dto) {
//        Page<NotificationResponseDTO> notifications = notificationService.getNotificationsByUserAndType(
//                dto.getUserId(), dto.getType(), dto.getPage(), dto.getSize());
//        return ResponseEntity.ok(notifications);
//    }
//
//
//}