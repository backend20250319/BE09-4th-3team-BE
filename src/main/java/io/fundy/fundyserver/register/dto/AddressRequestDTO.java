package io.fundy.fundyserver.register.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddressRequestDTO {

    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "연락처는 필수입니다.")
    private String phone;

    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipcode;

    @NotBlank(message = "기본주소는 필수입니다.")
    private String address;

    private String detail;

    // 기본 배송지 여부 (선택사항)
    @Builder.Default
    private boolean isDefault = false;
}