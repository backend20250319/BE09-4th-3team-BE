package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminProjectResponseDto;
import io.fundy.fundyserver.admin.service.AdminProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final AdminProjectService adminProjectService;

    /**
     * 전체 프로젝트 조회 (카테고리 필터 가능)
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param categoryNo 카테고리 ID (선택)
     * @return 페이징된 프로젝트 목록
     */
    @GetMapping
    public ResponseEntity<Page<AdminProjectResponseDto>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long categoryNo
    ) {
        Page<AdminProjectResponseDto> result = adminProjectService.getAllProjects(page, categoryNo);
        return ResponseEntity.ok(result);
    }
}
