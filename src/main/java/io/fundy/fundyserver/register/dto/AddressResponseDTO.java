package io.fundy.fundyserver.register.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddressResponseDTO {
    private Long id;
    private String name;
    private String phone;
    private String zipcode;
    private String address;
    private String detail;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}