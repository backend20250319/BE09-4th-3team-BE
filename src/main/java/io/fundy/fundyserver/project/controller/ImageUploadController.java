package io.fundy.fundyserver.project.controller;

import io.fundy.fundyserver.project.dto.image.ImageDTO;
import io.fundy.fundyserver.project.dto.image.ImageUploadResponseDTO;
import io.fundy.fundyserver.project.service.ImageService;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ImageUploadController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ImageDTO original = imageService.saveOriginalImage(file);
        ImageDTO thumbnail = imageService.generateThumbnail(original);

        return ResponseEntity.ok(
                ImageUploadResponseDTO.builder()
                        .image(original)
                        .thumbnail(thumbnail)
                        .build()
        );
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) throws IOException {
        Path imagePath = Paths.get("C:/dev/BE09-4th-3team-Image", filename);
        Resource resource = new UrlResource(imagePath.toUri());

        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }

    @GetMapping("/images/thumb/{filename:.+}")
    public ResponseEntity<Resource> serveThumbnail(@PathVariable String filename) throws IOException {
        Path thumbPath = Paths.get("C:/dev/BE09-4th-3team-Image/thumb", filename);
        Resource resource = new UrlResource(thumbPath.toUri());

        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }
}

