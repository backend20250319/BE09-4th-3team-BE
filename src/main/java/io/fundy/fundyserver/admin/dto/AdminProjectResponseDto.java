package io.fundy.fundyserver.admin.dto;

import io.fundy.fundyserver.project.entity.Project;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProjectResponseDto {

    private Long projectNo;
    private String title;
    private String description;
    private String accountNumber;      // 새로 추가된 필드
    private Integer goalAmount;
    private Integer currentAmount;
    private LocalDate startLine;
    private LocalDate deadLine;
    private String productStatus;
    private String thumbnailUrl;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String userId;        // 창작자 로그인 ID
    private String categoryName;  // 카테고리 이름

    public static AdminProjectResponseDto fromEntity(Project project) {
        return AdminProjectResponseDto.builder()
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .description(project.getDescription())
                .accountNumber(project.getAccountNumber()) // ✅ 추가된 부분
                .goalAmount(project.getGoalAmount())
                .currentAmount(project.getCurrentAmount())
                .startLine(project.getStartLine())
                .deadLine(project.getDeadLine())
                .productStatus(project.getProductStatus().name())
                .thumbnailUrl(project.getThumbnailUrl())
                .viewCount(project.getViewCount())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .userId(project.getUser().getUserId())
                .categoryName(project.getCategory().getName())
                .build();
    }
}
