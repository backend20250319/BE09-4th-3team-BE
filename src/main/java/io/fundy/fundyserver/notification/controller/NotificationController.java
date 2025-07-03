package io.fundy.fundyserver.notification.controller;


import io.fundy.fundyserver.notification.dto.NotificationResponseDTO;
import io.fundy.fundyserver.notification.service.NotificationService;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/support")
    public ResponseEntity<String> sendSupport(
            @RequestParam Integer userNo,
            @RequestParam Long projectNo,
            @RequestParam String projectTitle) {
        notificationService.sendSupportComplete(userNo, projectNo, projectTitle);
        return ResponseEntity.ok("후원 완료 알림이 성공적으로 전송되었습니다.");
    }

    @PostMapping("/success")
    public ResponseEntity<String> sendSuccess(
            @RequestParam Integer userNo,
            @RequestParam Long projectNo,
            @RequestParam String projectTitle) {
        try {
            notificationService.sendProjectSuccess(userNo, projectNo, projectTitle);
            return ResponseEntity.ok("프로젝트 성공 알림이 성공적으로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            // 참여 권한 없는 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/fail")
    public ResponseEntity<String> sendFail(
            @RequestParam Integer userNo,
            @RequestParam Long projectNo,
            @RequestParam String projectTitle) {
        try {
            notificationService.sendProjectFail(userNo, projectNo, projectTitle);
            return ResponseEntity.ok("프로젝트 실패 알림이 성공적으로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            // 참여 권한 없는 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 알림 목록 조회
//    @GetMapping
//    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
//            @AuthenticationPrincipal UserDetails userDetails, // 다운캐스팅해서 사용
//            @RequestParam(defaultValue = "all") String type) {
//
//        // CustomUserDetails로 다운캐스팅
//        if (!(userDetails instanceof CustomUserDetails customUser)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        Integer userNo = customUser.getUser().getUserNo();
//        List<NotificationResponseDTO> notifications = notificationService.getNotificationsByUserAndType(userNo, type);
//
//        return ResponseEntity.ok(notifications);
//    }

    // 인증 없이 userNo 쿼리로 알림 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @RequestParam Integer userNo,
            @RequestParam(defaultValue = "all") String type) {

        List<NotificationResponseDTO> notifications = notificationService.getNotificationsByUserAndType(userNo, type);
        return ResponseEntity.ok(notifications);
    }
}