package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminUserResponseDto;
import io.fundy.fundyserver.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 관리자 사용자 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param nickname 닉네임 검색 키워드 (선택)
     */
    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String nickname
    ) {
        Page<AdminUserResponseDto> result = adminUserService.getUserList(page, nickname);
        return ResponseEntity.ok(result);
    }
}
