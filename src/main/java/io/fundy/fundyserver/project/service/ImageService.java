package io.fundy.fundyserver.project.service;

import io.fundy.fundyserver.project.dto.image.ImageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.base-url}")
    private String baseUrl;

    @Value("${upload.port}")
    private int ftpPort;

    @Value("${upload.userId}")
    private String ftpUser;

    @Value("${upload.password}")
    private String ftpPassword;

    // 에디터 전용 이미지 저장 및 FTP 전송
    public ImageDTO saveEditorImage(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originName = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> Paths.get(name).getFileName().toString())
                .orElse("unknown.png");

        String extension = getExtension(originName);
        String savedName = uuid + extension;

        // 임시 파일 생성
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Files.createDirectories(tempDir);
        File tempFile = tempDir.resolve(savedName).toFile();
        file.transferTo(tempFile);

        // FTP 업로드
        uploadToFtp("/images/2/editor", savedName, tempFile);

        String imageUrl = baseUrl + ":8008" + "/images/2/editor/" + savedName;
        return new ImageDTO(originName, savedName, imageUrl, "에디터 이미지");
    }

    // 썸네일 생성 및 FTP 업로드
    public ImageDTO generateThumbnail(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        String thumbFileName = "thumb_" + uuid + extension;

        // 썸네일 임시 저장 디렉토리 생성
        Path thumbDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Files.createDirectories(thumbDir);

        File thumbnailFile = thumbDir.resolve(thumbFileName).toFile();

        // 이미지 원본 읽기
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        Thumbnails.of(originalImage)
                .sourceRegion(x, y, size, size)
                .size(300, 300)
                .keepAspectRatio(false)
                .toFile(thumbnailFile);

        // FTP 업로드
        uploadToFtp("/images/2/thumb", thumbFileName, thumbnailFile);

        return new ImageDTO(
                originalFilename,
                thumbFileName,
                baseUrl + ":8008" + "/images/2/thumb/" + thumbFileName,
                "썸네일 이미지"
        );
    }

    private void uploadToFtp(String remotePath, String filename, File file) throws IOException {
        FTPClient ftp = new FTPClient();
        try (InputStream input = new FileInputStream(file)) {
            ftp.connect(baseUrl.replace("http://", ""), ftpPort);
            ftp.login(ftpUser, ftpPassword);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            ftp.makeDirectory(remotePath);
            ftp.changeWorkingDirectory(remotePath);
            ftp.storeFile(filename, input);
        } finally {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return "";
        return fileName.substring(dotIndex);
    }
}