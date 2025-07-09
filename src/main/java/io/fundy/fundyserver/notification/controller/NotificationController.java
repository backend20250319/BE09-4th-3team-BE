package io.fundy.fundyserver.notification.controller;


import io.fundy.fundyserver.notification.dto.NotificationRequestDTO;
import io.fundy.fundyserver.notification.dto.NotificationResponseDTO;
import io.fundy.fundyserver.notification.dto.NotificationSendRequestDTO;
import io.fundy.fundyserver.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    /**
     * 후원 완료 알림 전송
     * @param dto 프로젝트 번호, 제목 포함 DTO
     * @param userId 인증된 사용자 ID (스프링 시큐리티에서 주입)
     * @return 성공 메시지
     */
    @PostMapping("/support")
    public ResponseEntity<String> sendSupport(
            @RequestBody NotificationSendRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        notificationService.sendSupportComplete(userId, dto.getProjectNo(), dto.getProjectTitle());
        return ResponseEntity.ok("후원 완료 알림이 성공적으로 전송되었습니다.");
    }

    /**
     * 프로젝트 성공 마감 알림 전송
     * @param dto 프로젝트 번호, 제목 포함 DTO
     * @param userId 인증된 사용자 ID
     * @return 성공 또는 에러 메시지와 상태 코드 반환
     */
    @PostMapping("/success")
    public ResponseEntity<String> sendSuccess(
            @RequestBody NotificationSendRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        try {
            notificationService.sendProjectSuccess(userId, dto.getProjectNo(), dto.getProjectTitle());
            return ResponseEntity.ok("프로젝트 성공 알림이 성공적으로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 프로젝트 실패 마감 알림 전송
     * @param dto 프로젝트 번호, 제목 포함 DTO
     * @param userId 인증된 사용자 ID
     * @return 성공 또는 에러 메시지와 상태 코드 반환
     */
    @PostMapping("/fail")
    public ResponseEntity<String> sendFail(
            @RequestBody NotificationSendRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        try {
            notificationService.sendProjectFail(userId, dto.getProjectNo(), dto.getProjectTitle());
            return ResponseEntity.ok("프로젝트 실패 알림이 성공적으로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 알림 삭제 (소프트 삭제)
     * @param notificationNo 삭제할 알림 ID
     * @param userId 인증된 사용자 ID (본인 알림만 삭제 가능)
     * @return 삭제 성공 시 204 No Content 반환
     */
    @PatchMapping("/{notificationNo}/delete")
    public ResponseEntity<String> softDeleteNotification(
            @PathVariable Long notificationNo,
            @AuthenticationPrincipal String userId
    ) {
        notificationService.deleteNotification(notificationNo, userId);
        return ResponseEntity.ok("알림이 삭제 처리되었습니다.");
    }

    /**
     * 읽지 않은 알림 개수 조회
     * @param userId 인증된 사용자 ID
     * @return 읽지 않은 알림 개수 반환
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal String userId) {
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 모든 알림을 읽음 처리
     * @param userId 인증된 사용자 ID
     * @return 처리 완료 메시지 반환
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<String> markAllAsRead(@AuthenticationPrincipal String userId) {
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok("모든 알림이 읽음 처리되었습니다.");
    }

    /**
     * 알림 목록 조회 (타입 및 페이지네이션 적용)
     * @param userId 인증된 사용자 ID
     * @param dto 조회 조건 DTO (타입, 페이지, 사이즈)
     * @return 페이징된 알림 목록 DTO 반환
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            @AuthenticationPrincipal String userId,
            NotificationRequestDTO dto
    ) {
        Page<NotificationResponseDTO> notifications = notificationService.getNotificationsByUserAndType(
                userId, dto.getType(), dto.getPage(), dto.getSize());
        return ResponseEntity.ok(notifications);
    }
}