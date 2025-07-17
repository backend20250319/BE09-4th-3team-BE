package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.EmailRequest;
import io.fundy.fundyserver.register.dto.EmailVerifyRequest;
import io.fundy.fundyserver.register.dto.EmailVerificationResult;
import io.fundy.fundyserver.register.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/register")
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    // 인증번호 메일 발송 요청
    @PostMapping("/send-auth-code")
    public String sendAuthCode(@RequestBody EmailRequest req) {
        emailVerificationService.sendVerificationCode(req.getEmail());
        return "인증번호 발송 완료";
    }

    // 인증번호 검증 요청
    @PostMapping("/verify-auth-code")
    public EmailVerificationResult verifyAuthCode(@RequestBody EmailVerifyRequest req) {
        return emailVerificationService.verifyCode(req.getEmail(), req.getAuthCode());
    }
}
