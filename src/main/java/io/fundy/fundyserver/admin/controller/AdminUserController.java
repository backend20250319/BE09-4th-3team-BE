package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminUserRequestDto;
import io.fundy.fundyserver.register.entity.UserStatus;
import io.fundy.fundyserver.register.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/status")
    public ResponseEntity<Void> updateUserStatus(@RequestBody AdminUserRequestDto dto) {
        // "BAN" → "BANNED"로 변환하여 Enum으로 매핑
        String statusInput = "BAN".equalsIgnoreCase(dto.getUserStatus())
                ? "BANNED"
                : dto.getUserStatus().toUpperCase();

        // 문자열 → Enum 안전하게 변환
        UserStatus userStatus = UserStatus.valueOf(statusInput);

        userService.updateUserStatus(dto.getUserId(), userStatus);
        return ResponseEntity.ok().build();
    }
}
