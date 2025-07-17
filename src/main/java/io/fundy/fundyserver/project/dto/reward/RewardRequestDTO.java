package io.fundy.fundyserver.project.dto.reward;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardRequestDTO {
    private String title;
    private Integer amount;
    private String description;
    private Integer stock; // null 또는 -1 가능
}
