package io.fundy.fundyserver.project.controller;

import io.fundy.fundyserver.project.dto.project.ProjectDetailResponseDTO;
import io.fundy.fundyserver.project.dto.project.ProjectListPageResponseDTO;
import io.fundy.fundyserver.project.dto.project.ProjectRequestDTO;
import io.fundy.fundyserver.project.dto.project.ProjectResponseDTO;
import io.fundy.fundyserver.project.service.ProjectService;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.aspectj.weaver.ast.HasAnnotation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /***
     * 프로젝트 등록
     * @param requestDTO
     * @param user
     * @return
     */
    @PreAuthorize("hasAuthority(\"USER\")")
    @PostMapping("/project")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO requestDTO,
                @AuthenticationPrincipal CustomUserDetails user
    ) {
        String userId = user.getUsername(); // 여기서 String 추출

        ProjectResponseDTO response = projectService.createService(requestDTO, userId);
        return ResponseEntity.status(201).body(response);
    }

    /***
     * 프로젝트 목록 조회
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/project/list")
    public ResponseEntity<?> getProjectList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        ProjectListPageResponseDTO response = projectService.getProjects(page, size);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "data", response.getData(),
                        "pagination", response.getPagination()
                )
        );
    }

    /***
     * 프로젝트 상세 조회
     * @param projectNo
     * @return
     */
    @GetMapping("/project/{projectNo}")
    public ResponseEntity<?> getProjectDetail(@PathVariable Long projectNo) {
        ProjectDetailResponseDTO responseDTO = projectService.getProjectById(projectNo);
        return ResponseEntity.ok().body(Map.of("success", true, "data", responseDTO));
    }
}
