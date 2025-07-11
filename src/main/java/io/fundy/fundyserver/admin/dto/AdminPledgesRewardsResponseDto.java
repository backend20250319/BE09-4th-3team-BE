package io.fundy.fundyserver.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminPledgesRewardsResponseDto {

    private Long pledgeRewardNo;      // 후원 리워드 ID

    private Long pledgeNo;            // 소속 후원 ID
    private String userName;          // 후원자 이름 또는 닉네임
    private String userEmail;         // 후원자 이메일
    private Long projectNo;           // 프로젝트 ID
    private String projectTitle;      // 프로젝트 제목

    private Long rewardNo;            // 리워드 ID (현재 기준)
    private String rewardTitle;       // 리워드 제목 (후원 시점 스냅샷)
    private Integer rewardAmount;     // 리워드 금액 (후원 시점 스냅샷)
    private Integer quantity;         // 선택 수량
    private Integer totalRewardAmount; // 리워드 금액 × 수량

    private String recipientName;     // 수령인 이름
    private String deliveryAddress;   // 배송 주소
    private String deliveryPhone;     // 배송 연락처

    private String createdAt;         // 후원 생성일 (문자열 포맷으로 표현할 수도 있음)
}
