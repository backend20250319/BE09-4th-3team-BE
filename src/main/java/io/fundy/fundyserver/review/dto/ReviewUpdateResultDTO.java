package io.fundy.fundyserver.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateResultDTO {
    private ReviewResponseDTO before;
    private ReviewResponseDTO after;
}