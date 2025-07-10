package io.fundy.fundyserver.register.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/profile_images")
public class ProfileImageController {

    private static final String BASE_PATH = "C:/profile_images/";

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String fileName) {
        try {
            // URL 디코딩 (한글, 공백, 특수문자 지원)
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
            File file = new File(BASE_PATH + decodedFileName);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // MIME 타입 지정 (여기선 단순 예시, 확장자별 분기 필요하면 별도 처리)
            String contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + decodedFileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
