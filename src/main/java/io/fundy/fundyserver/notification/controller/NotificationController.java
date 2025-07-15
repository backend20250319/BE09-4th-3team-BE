package io.fundy.fundyserver.notification.controller;


import io.fundy.fundyserver.notification.dto.NotificationRequestDTO;
import io.fundy.fundyserver.notification.dto.NotificationResponseDTO;
import io.fundy.fundyserver.notification.dto.NotificationSendRequestDTO;
import io.fundy.fundyserver.notification.service.NotificationService;
import io.fundy.fundyserver.project.service.ProjectService;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
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
    private final ProjectService projectService;
    private final UserRepository userRepository;


    /**
     * 후원 완료 알림 전송
     * @param dto 프로젝트 번호, 제목 포함 DTO
     * @param userId 인증된 사용자 ID (스프링 시큐리티가 자동 주입)
     * @return 성공 메시지 반환
     */
    @PostMapping("/support")
    public ResponseEntity<String> sendSupport(
            @RequestBody NotificationSendRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        // 사용자 닉네임 조회, 없으면 "알 수 없는 사용자"로 대체
        String supporterName = userRepository.findByUserId(userId)
                .map(User::getNickname)
                .orElse("알 수 없는 사용자");

        // 후원 완료 알림을 프로젝트 서비스에 위임 (직접 NotificationService 호출하지 않음)
        projectService.sendSupportCompleteNotification(userId, dto.getProjectNo(), dto.getProjectTitle(), supporterName);

        return ResponseEntity.ok("후원 완료 알림이 성공적으로 전송되었습니다.");
    }

    /**
     * 프로젝트 성공 알림 전송
     * @param dto 프로젝트 정보 포함 DTO
     * @param userId 인증된 사용자 ID
     * @return 성공 메시지 반환
     */
    @PostMapping("/success")
    public ResponseEntity<String> sendSuccess(
            @RequestBody NotificationSendRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        // 프로젝트 성공 알림을 프로젝트 서비스에 위임
        projectService.sendProjectSuccessNotification(userId, dto.getProjectNo(), dto.getProjectTitle());
        return ResponseEntity.ok("프로젝트 성공 알림이 성공적으로 전송되었습니다.");
    }

    /**
     * 프로젝트 실패 알림 전송
     * @param dto 프로젝트 정보 포함 DTO
     * @param userId 인증된 사용자 ID
     * @return 성공 메시지 반환
     */
    @PostMapping("/fail")
    public ResponseEntity<String> sendFail(
            @RequestBody NotificationSendRequestDTO dto,
            @AuthenticationPrincipal String userId
    ) {
        // 프로젝트 실패 알림을 프로젝트 서비스에 위임
        projectService.sendProjectFailNotification(userId, dto.getProjectNo(), dto.getProjectTitle());
        return ResponseEntity.ok("프로젝트 실패 알림이 성공적으로 전송되었습니다.");
    }

    /**
     * 알림 소프트 삭제 (삭제 처리 표시만 함)
     * @param notificationNo 삭제할 알림 ID
     * @param userId 인증된 사용자 ID (본인 알림만 삭제 가능)
     * @return 삭제 완료 메시지 반환
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
     * 알림 목록 조회 (타입별, 페이징 적용)
     * @param userId 인증된 사용자 ID
     * @param dto 조회 조건 DTO (알림 타입, 페이지 번호, 페이지 사이즈)
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