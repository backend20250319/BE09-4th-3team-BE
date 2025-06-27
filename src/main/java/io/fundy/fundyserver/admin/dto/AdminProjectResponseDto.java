package io.fundy.fundyserver.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProjectResponseDto {

    private Integer no;              // 목록 순번
    private Long id;                 // 프로젝트 ID
    private String title;            // 프로젝트 제목
    private String userId;             // 창작자 ID
    private Integer categoryId;      // 카테고리 ID
    private Integer goalAmount;      // 목표 금액
    private Integer currentAmount;   // 현재 후원 금액
    private String deadline;         // 마감일
    private String productStatus;    // 프로젝트 상태
    private Integer viewCount;       // 조회수
    private String thumbnailUrl;     // 썸네일 URL
    private String createdAt;        // 생성일
    private String updatedAt;        // 수정일
}