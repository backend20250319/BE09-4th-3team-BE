package io.fundy.fundyserver.pledge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PledgeRequestDTO {

    @NotNull(message = "프로젝트 번호는 필수입니다")
    private Long projectNo; // 후원할 프로젝트 번호

    @NotEmpty(message = "최소 하나 이상의 리워드를 선택해야 합니다")
    @Valid
    private List<PledgeRewardDTO> rewards; // 선택한 리워드 목록

    @Min(value = 0, message = "추가 후원금은 0 이상이어야 합니다")
    private Integer additionalAmount = 0; // 추가 후원금

    @NotBlank(message = "배송지 주소는 필수입니다")
    private String deliveryAddress; // 배송지 주소

    @NotBlank(message = "배송 연락처는 필수입니다")
    private String deliveryPhone; // 배송 연락처

    @NotBlank(message = "수령인 이름은 필수입니다")
    private String recipientName; // 수령인 이름

    @Getter
    @Setter
    public static class PledgeRewardDTO {
        @NotNull(message = "리워드 번호는 필수입니다")
        private Long rewardNo; // 리워드 번호

        @NotNull(message = "리워드 수량은 필수입니다")
        @Min(value = 1, message = "리워드 수량은 최소 1개 이상이어야 합니다")
        private Integer quantity = 1; // 리워드 수량
    }
}
