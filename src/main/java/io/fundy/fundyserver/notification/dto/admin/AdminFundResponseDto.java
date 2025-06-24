package io.fundy.fundyserver.notification.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFundResponseDto {

    private Integer no;              // 순번
    private Long id;                 // 후원 ID
    private Long userId;             // 후원자 ID user테이블에서 join해서 이름 가져오기
    private Long projectId;          // 프로젝트 ID
    private Long rewardId;           // 리워드 ID
    private Integer amount;          // 후원 금액
    private String status;           // 후원 상태 (PENDING, CONFIRMED, CANCELLED)
    private String createdAt;        // 후원 생성 시각
    private String confirmedAt;      // 확정 시각 (nullable)
    private String cancelReason;     // 취소 사유 (nullable)
}

