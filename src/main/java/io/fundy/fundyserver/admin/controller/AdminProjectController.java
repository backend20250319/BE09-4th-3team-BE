package io.fundy.fundyserver.admin.controller;

import io.fundy.fundyserver.admin.dto.AdminProjectRequestDto;
import io.fundy.fundyserver.admin.dto.AdminProjectResponseDto;
import io.fundy.fundyserver.admin.dto.AdminTotalProjectDto;
import io.fundy.fundyserver.admin.service.AdminProjectService;
import io.fundy.fundyserver.project.entity.ProjectStatus;
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

    /**
     * 프로젝트 상태 변경 (승인/거절)
     *
     * @param dto 프로젝트 ID와 변경할 상태값
     */
    @PostMapping("/status")
    public ResponseEntity<Void> updateProjectStatus(@RequestBody AdminProjectRequestDto dto) {
        adminProjectService.updateProjectStatus(dto.getProjectId(), ProjectStatus.valueOf(dto.getProductStatus()));
        return ResponseEntity.ok().build();
    }

    /*전체 프로젝트 카운트*/
    @GetMapping("/count")
    public ResponseEntity<AdminTotalProjectDto> getProjectStatistics() {
        AdminTotalProjectDto stats = adminProjectService.getProjectStatistics();
        return ResponseEntity.ok(stats);
    }


}
