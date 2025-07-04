package io.fundy.fundyserver.project.controller;

import io.fundy.fundyserver.project.dto.image.ImageDTO;
import io.fundy.fundyserver.project.dto.image.ImageUploadResponseDTO;
import io.fundy.fundyserver.project.service.ImageService;
import io.fundy.fundyserver.register.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/project")
public class ImageUploadController {

    private final ImageService imageService;

    // ✅ 썸네일 이미지만 저장 (원본 이미지는 저장하지 않음)
    @PostMapping("/images/upload")
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        // 원본 저장 없이 썸네일만 생성 및 저장
        ImageDTO thumbnail = imageService.generateThumbnail(file);

        return ResponseEntity.ok(
                ImageUploadResponseDTO.builder()
                        .thumbnail(thumbnail)  // 썸네일 정보만 반환
                        .build()
        );
    }


    // ✅ CKEditor 전용 이미지 업로드 (썸네일 X)
    @PostMapping("/images/ckeditor-upload")
    public ResponseEntity<?> uploadEditorImage(@RequestParam("upload") MultipartFile file) throws IOException {
        ImageDTO image = imageService.saveEditorImage(file);

        return ResponseEntity.ok(Map.of(
                "uploaded", true,
                "url", image.getImagePath()
        ));
    }
}
