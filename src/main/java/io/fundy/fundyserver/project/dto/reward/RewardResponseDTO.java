package io.fundy.fundyserver.project.dto.reward;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponseDTO {
    private Long id;
    private String title;
    private Integer amount;
    private String description;
    private Integer stock;
}
