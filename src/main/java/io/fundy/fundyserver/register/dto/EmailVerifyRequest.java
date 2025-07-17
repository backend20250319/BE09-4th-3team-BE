package io.fundy.fundyserver.register.dto;
import lombok.Getter;
@Getter
public class EmailVerifyRequest {
    private String email;
    private String authCode;
}
