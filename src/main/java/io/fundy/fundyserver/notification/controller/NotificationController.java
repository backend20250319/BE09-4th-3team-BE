package io.fundy.fundyserver.notification.controller;


import io.fundy.fundyserver.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ① 후원 완료 알림 전송
    @PostMapping("/support")
    public ResponseEntity<Void> sendSupport(
            @RequestParam Integer userNo,
            @RequestParam Long projectNo,
            @RequestParam String projectTitle) {
        notificationService.sendSupportComplete(userNo, projectNo, projectTitle);
        return ResponseEntity.ok().build();
    }

    // ② 프로젝트 성공 마감 알림 전송
    @PostMapping("/success")
    public ResponseEntity<Void> sendSuccess(
            @RequestParam Integer userNo,
            @RequestParam Long projectNo,
            @RequestParam String projectTitle) {
        notificationService.sendProjectSuccess(userNo, projectNo, projectTitle);
        return ResponseEntity.ok().build();
    }

    // ③ 프로젝트 실패 마감 알림 전송
    @PostMapping("/fail")
    public ResponseEntity<Void> sendFail(
            @RequestParam Integer userNo,
            @RequestParam Long projectNo,
            @RequestParam String projectTitle) {
        notificationService.sendProjectFail(userNo, projectNo, projectTitle);
        return ResponseEntity.ok().build();
    }
}