package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.EmailVerificationResult;
import io.fundy.fundyserver.register.dto.SingleResponseDto;
import io.fundy.fundyserver.register.service.MemberService;
import io.fundy.fundyserver.register.validation.CustomEmail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MailController {
    private final MemberService memberService;

    @PostMapping("/emails/verification-requests")
    public ResponseEntity sendMessage(@RequestParam("email") @Valid @CustomEmail String email) {
        memberService.sendCodeToEmail(email);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/emails/verifications")
    public ResponseEntity verificationEmail(@RequestParam("email") @Valid @CustomEmail String email,
                                            @RequestParam("code") String authCode) {
        EmailVerificationResult response = memberService.verifiedCode(email, authCode);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }
}
