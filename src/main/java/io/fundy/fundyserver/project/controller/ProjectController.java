package io.fundy.fundyserver.project.controller;

import io.fundy.fundyserver.project.dto.project.ProjectDetailResponseDTO;
import io.fundy.fundyserver.project.dto.project.ProjectListPageResponseDTO;
import io.fundy.fundyserver.project.dto.project.ProjectRequestDTO;
import io.fundy.fundyserver.project.dto.project.ProjectResponseDTO;
import io.fundy.fundyserver.project.service.ProjectService;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/project")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /***
     * 프로젝트 등록
     * @param requestDTO
     * @return
     */
    @PostMapping("/upload")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO requestDTO,
            @AuthenticationPrincipal String user
    ) {

        String userId = user;

        ProjectResponseDTO response = projectService.createService(requestDTO, userId);
        return ResponseEntity.status(201).body(response);
    }

    /***
     * 프로젝트 목록 조회
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<?> getProjectList(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ProjectListPageResponseDTO response = projectService.getProjects(pageable);

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
    @GetMapping("/{projectNo}")
    public ResponseEntity<?> getProjectDetail(@PathVariable Long projectNo) {
        ProjectDetailResponseDTO responseDTO = projectService.getProjectById(projectNo);
        return ResponseEntity.ok().body(Map.of("success", true, "data", responseDTO));
    }
}
