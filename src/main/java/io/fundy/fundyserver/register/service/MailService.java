// 이메일 인증을 위한
package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.exception.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.fundy.fundyserver.register.exception.ErrorCode;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;

    public static void sendEmailStatic(JavaMailSender mailSender, String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Fundy 이메일 인증 번호");
        message.setText( "인증번호는 " + text + " 입니다.");
        mailSender.send(message);
    }

    public void sendEmail(String toEmail,
                          String title,
                          String text) {
        SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);
        try {
            emailSender.send(emailForm);
        } catch (RuntimeException e) {
            log.debug("MailService.sendEmail exception occur toEmail: {}, " +
                    "title: {}, text: {}", toEmail, title, text);
            throw new BusinessLogicException(ErrorCode.UNABLE_TO_SEND_EMAIL);
        }
    }

    // 발신할 이메일 데이터 세팅
    private SimpleMailMessage createEmailForm(String toEmail,
                                              String title,
                                              String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);

        return message;
    }
}
