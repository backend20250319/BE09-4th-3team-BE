package io.fundy.fundyserver.project.service;

import io.fundy.fundyserver.project.dto.image.ImageDTO;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.base-url}")
    private String baseUrl;

    // 에디터 전용 이미지 저장
    public ImageDTO saveEditorImage(MultipartFile file) throws IOException {
        return saveImage(file, "editor");
    }

    // 일반 업로드 이미지 저장
//    public ImageDTO saveOriginalImage(MultipartFile file) throws IOException {
//        return saveImage(file, "");
//    }

    // 썸네일 생성
    public ImageDTO generateThumbnail(MultipartFile file) throws IOException {
        // 썸네일 저장 디렉토리 생성
        Path thumbDir = Paths.get(uploadPath, "thumb");
        Files.createDirectories(thumbDir);

        // UUID 기반 파일명 생성 (확장자 유지)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        String thumbFileName = "thumb_" + uuid + extension;

        // 썸네일 파일 경로 지정
        File thumbnailFile = thumbDir.resolve(thumbFileName).toFile();

        // MultipartFile → 썸네일 생성 및 저장
        Thumbnails.of(file.getInputStream())
                .size(300, 300)
                .toFile(thumbnailFile);

        // 결과 반환
        return new ImageDTO(
                originalFilename,
                thumbFileName,
                baseUrl + "/images/thumb/" + thumbFileName,
                "썸네일 이미지"
        );
    }


    private ImageDTO saveImage(MultipartFile file, String subDir) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originName = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> Paths.get(name).getFileName().toString())
                .orElse("unknown.png");

        String extension = getExtension(originName);
        String savedName = uuid + extension;

        Path dirPath = subDir.isEmpty()
                ? Paths.get(uploadPath)
                : Paths.get(uploadPath, subDir);

        Files.createDirectories(dirPath);

        Path imagePath = dirPath.resolve(savedName);
        file.transferTo(imagePath.toFile());

        String imageUrl = baseUrl + "/images/" + (subDir.isEmpty() ? "" : subDir + "/") + savedName;

        return new ImageDTO(originName, savedName, imageUrl, "저장된 이미지");
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return "";
        return fileName.substring(dotIndex);
    }
}
