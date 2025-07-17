package io.fundy.fundyserver.project.dto.project;

import io.fundy.fundyserver.project.dto.reward.RewardRequestDTO;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestDTO {

    @NotBlank(message = "프로젝트 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "상세 설명은 필수입니다.")
    private String description;

    @NotNull(message = "목표 금액은 필수입니다.")
    @Min(value = 500000, message = "최소 50만원 이상이어야 합니다.")
    private Integer goalAmount;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startLine;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate deadLine;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryNo;

    @NotBlank(message = "계좌번호는 필수입니다.")
    private String accountNumber;

    @NotBlank(message = "창작자 이름은 필수입니다.")
    private String creatorName;

    @NotBlank(message = "창작자 소개는 필수입니다.")
    private String creatorInfo;

    @NotBlank(message = "썸네일 URL은 필수입니다.")
    private String thumbnailUrl;

    private List<RewardRequestDTO> rewards;
}
