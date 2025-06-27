package io.fundy.fundyserver.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotificationResponseDto {

    private Integer no;           // 목록 순번
    private Long reviewId;        // 후기 ID
    private Long projectId;       // 프로젝트 ID
    private String userId;        // 작성자(후원자) ID
    private Integer rating;       // 평점 (1~5)
    private String content;       // 후기 내용
    private String createdAt;     // 작성일 (yyyy-MM-dd HH:mm:ss)
    private String updatedAt;     // 수정일
}
