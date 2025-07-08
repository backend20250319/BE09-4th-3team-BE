package io.fundy.fundyserver.register.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailVerificationResult {
    private boolean success;
    private String message;

    // ✅ of 메서드 추가
    public static EmailVerificationResult of(boolean authResult) {
        if (authResult) {
            return new EmailVerificationResult(true, "인증 성공");
        } else {
            return new EmailVerificationResult(false, "인증 실패 또는 코드 만료");
        }
    }
}
