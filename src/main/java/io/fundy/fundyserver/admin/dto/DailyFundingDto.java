package io.fundy.fundyserver.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyFundingDto {
    private String date;          // "yyyy-MM-dd"
    private Integer totalFunding; // 일자별 총 후원금
}

