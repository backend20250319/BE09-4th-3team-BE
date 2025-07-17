package io.fundy.fundyserver.project.dto.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ImageDTO {
    private String originFileName;   // 원본 파일명
    private String savedFileName;    // 저장된 파일명 (예: UUID 포함)
    private String imagePath;         // 전체 파일 경로
    private String imageDescription;  // 파일 설명

}

