package io.fundy.fundyserver.pledge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PledgeRequestDTO {

    @NotNull(message = "프로젝트 번호는 필수입니다")
    private Long projectNo; // 후원할 프로젝트 번호

    @NotNull(message = "리워드 번호는 필수입니다")
    private Long rewardNo; // 선택한 리워드 번호

    @Min(value = 0, message = "추가 후원금은 0 이상이어야 합니다")
    private Integer additionalAmount = 0; // 추가 후원금

    @NotBlank(message = "배송지 주소는 필수입니다")
    private String deliveryAddress; // 배송지 주소

    @NotBlank(message = "배송 연락처는 필수입니다")
    private String deliveryPhone; // 배송 연락처

    @NotBlank(message = "수령인 이름은 필수입니다")
    private String recipientName; // 수령인 이름
}
