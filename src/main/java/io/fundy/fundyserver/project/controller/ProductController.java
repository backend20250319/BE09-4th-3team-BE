//package io.fundy.fundyserver.project.controller;
//
//import io.fundy.fundyserver.project.dto.project.ProjectRequestDTO;
//import io.fundy.fundyserver.project.dto.project.ProjectResponseDTO;
//import io.fundy.fundyserver.project.service.ProjectService;
//import io.fundy.fundyserver.register.security.CustomUserDetails;
//import jakarta.validation.Valid;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api")
//@AllArgsConstructor
//public class ProductController {
//
//    private final ProjectService projectService;
//
//    @PostMapping("/project")
//    public ResponseEntity<ProjectResponseDTO> createProject(
//            @Valid @RequestBody ProjectRequestDTO requestDTO,
//            @AuthenticationPrincipal CustomUserDetails user
//    ) {
//        String userId = user.getUsername(); // 여기서 String 추출
//
//        ProjectResponseDTO response = projectService.createService(requestDTO, userId);
//        return ResponseEntity.status(201).body(response);
//    }
//
//}
