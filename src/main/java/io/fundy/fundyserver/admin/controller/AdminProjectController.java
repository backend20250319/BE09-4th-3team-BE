//package io.fundy.fundyserver.admin.controller;
//
//
//import io.fundy.fundyserver.admin.dto.AdminProjectRequestDto;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@PostMapping("/admin/projects/status")
//public ResponseEntity<Void> updateProjectStatus(@RequestBody AdminProjectRequestDto dto) {
//    projectService.updateStatus(dto.getProjectId(), dto.getProductStatus());
//    return ResponseEntity.ok().build();
//}