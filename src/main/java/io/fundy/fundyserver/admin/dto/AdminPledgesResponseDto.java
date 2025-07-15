package io.fundy.fundyserver.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminPledgesResponseDto {

    private Long pledgeNo;              // 후원 ID
    private String userName;            // 후원자 닉네임
    private String userEmail;           // 후원자 이메일
    private String projectTitle;        // 후원한 프로젝트명

    private Integer totalAmount;        // 총 후원 금액
    private Integer additionalAmount;   // 추가 후원 금액
    private String createdAt;           // 후원 날짜 (yyyy-MM-dd HH:mm)

    private String recipientName;       // 수령인
    private String deliveryAddress;     // 배송 주소 (옵션)
    private String deliveryPhone;       // 배송 연락처 (옵션)
}
