package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.EmailVerificationResult;
import io.fundy.fundyserver.register.exception.BusinessLogicException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    private static final long RESEND_INTERVAL_SEC = 60; // 1분 제한
    private static final int DAILY_LIMIT = 5; // 하루 최대 5회

    @Value("${spring.mail.auth-code-expiration-millis:1800000}")
    private long authCodeExpirationMillis;

    public void sendVerificationCode(String email) {
        // 1. 이미 가입된 이메일이면 예외
        if (userRepository.existsByEmail(email)) {
            throw new BusinessLogicException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 1분 이내 재발송 제한
        String lastRequestKey = email + ":auth:lastRequest";
        String lastRequestTime = redisTemplate.opsForValue().get(lastRequestKey);
        long now = System.currentTimeMillis();
        if (lastRequestTime != null) {
            long last = Long.parseLong(lastRequestTime);
            if (now - last < RESEND_INTERVAL_SEC * 1000) {
                throw new BusinessLogicException(ErrorCode.TOO_FREQUENT_REQUEST);
            }
        }

        // 3. 하루 5회 제한
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String dailyCountKey = email + ":auth:count:" + date;
        String countStr = redisTemplate.opsForValue().get(dailyCountKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        if (count >= DAILY_LIMIT) {
            throw new BusinessLogicException(ErrorCode.DAILY_REQUEST_LIMIT_EXCEEDED);
        }

        // 4. 인증코드 생성 및 저장
        String code = generateRandomCode();
        redisTemplate.opsForValue().set(email, code, authCodeExpirationMillis, TimeUnit.MILLISECONDS);

        // 5. 요청 시간/횟수 저장
        redisTemplate.opsForValue().set(lastRequestKey, String.valueOf(now), RESEND_INTERVAL_SEC, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(dailyCountKey, String.valueOf(count + 1), Duration.ofDays(1));

        // 6. 메일 발송
        String subject = "Fundy 회원가입 인증번호 발송의 건";
        String text = "인증번호: " + code + "인증번호 요청은 하루 5회로 제한합니다. ." ;
        try {
            MailService.sendEmailStatic(mailSender, email, subject, text);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("이메일 발송 중 예외 발생 (EmailVerificationService)", e);
            throw new BusinessLogicException(ErrorCode.UNABLE_TO_SEND_EMAIL);
        }
    }

    public EmailVerificationResult verifyCode(String email, String inputCode) {
        String stored = redisTemplate.opsForValue().get(email);
        if (stored != null && stored.equals(inputCode)) {
            redisTemplate.delete(email);
            return new EmailVerificationResult(true, "인증 성공");
        } else if (stored == null) {
            return new EmailVerificationResult(false, "인증번호가 만료되었습니다. 재발급 요청 바랍니다.");
        } else {
            return new EmailVerificationResult(false, "인증번호가 일치하지 않습니다.");
        }
    }

    private String generateRandomCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
