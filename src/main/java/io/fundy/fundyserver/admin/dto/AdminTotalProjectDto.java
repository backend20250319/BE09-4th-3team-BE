package io.fundy.fundyserver.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminTotalProjectDto {
    private long total;
    private long pending;
    private long approved;
    private long rejected;
}
