package io.fundy.fundyserver.pledge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MyPledgeResponseDTO {
    private Long pledgeNo;
    private Long projectNo;
    private String projectTitle;
    private String rewardTitle;
    private Integer totalAmount;
    private String deliveryAddress;
    private String deliveryPhone;
    private String recipientName;
    private LocalDateTime createdAt;
}
