package io.fundy.fundyserver.project.controller;

import io.fundy.fundyserver.project.dto.project.ProjectListPageResponseDTO;
import io.fundy.fundyserver.project.dto.project.ProjectRequestDTO;
import io.fundy.fundyserver.project.dto.project.ProjectResponseDTO;
import io.fundy.fundyserver.project.service.ProjectService;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/project")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO requestDTO,
                @AuthenticationPrincipal CustomUserDetails user
    ) {
        String userId = user.getUsername(); // 여기서 String 추출

        ProjectResponseDTO response = projectService.createService(requestDTO, userId);
        return ResponseEntity.status(201).body(response);
    }

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

}
