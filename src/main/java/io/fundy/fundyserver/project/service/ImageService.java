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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    @Value("${upload.path}")
    private String uploadPath;

    // 파일 서버 URL 전달 받은 뒤 작업 할 내용
//    @Value("${upload.base-url}")
//    private String baseUrl; // ✅ 로컬 or 파일서버 URL

    public ImageDTO saveOriginalImage(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originName = file.getOriginalFilename();
        String savedName = uuid + "_" + originName;

        Path imageDir = Paths.get(uploadPath);
        Files.createDirectories(imageDir);

        Path imagePath = imageDir.resolve(savedName);
        file.transferTo(imagePath.toFile());

        System.out.println("savedName = " + savedName);

        return new ImageDTO(
                originName,
                savedName,
                "/projects/images/" + savedName,
                // baseUrl + "/" + savedName, // ✅ full URL 제공
                "원본 이미지"
        );
    }

    public ImageDTO generateThumbnail(ImageDTO originalFileDTO) throws IOException {
        Path thumbDir = Paths.get(uploadPath, "thumb");
        Files.createDirectories(thumbDir);

        File originalFile = Paths.get(uploadPath, originalFileDTO.getSavedFileName()).toFile();
        String thumbFileName = "thumb_" + originalFileDTO.getSavedFileName();
        File thumbnailFile = thumbDir.resolve(thumbFileName).toFile();

        System.out.println("thumbFileName = " + thumbFileName);

        Thumbnails.of(originalFile)
                .size(300, 300)
                .toFile(thumbnailFile);

        return new ImageDTO(
                originalFileDTO.getOriginFileName(),
                thumbFileName,
                "/projects/images/thumb/" + thumbFileName,
                // baseUrl + "/thumb/" + thumbFileName,
                "썸네일 이미지"
        );
    }
}
