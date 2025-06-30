package io.fundy.fundyserver.project.dto.project;

import io.fundy.fundyserver.project.dto.reward.RewardRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestDTO {
    private String title;
    private String description;
    private Integer goalAmount;
    private LocalDate startLine;
    private LocalDate deadLine;
    private Long categoryNo;
    private String accountNumber;
    private List<RewardRequestDTO> rewards;
}

