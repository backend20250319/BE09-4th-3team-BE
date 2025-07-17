package io.fundy.fundyserver.project.dto.image;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageUploadResponseDTO {
    private ImageDTO image;
    private ImageDTO thumbnail;
}

