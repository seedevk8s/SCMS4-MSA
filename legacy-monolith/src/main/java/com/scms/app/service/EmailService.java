package com.scms.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 이메일 발송 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${server.domain:http://localhost:8080}")
    private String serverDomain;

    /**
     * 이메일 인증 메일 발송
     *
     * @param toEmail 수신자 이메일
     * @param name 수신자 이름
     * @param token 인증 토큰
     */
    public void sendVerificationEmail(String toEmail, String name, String token) {
        try {
            String subject = "[푸름대학교 SCMS] 이메일 인증을 완료해주세요";

            // 인증 링크 생성
            String verificationLink = serverDomain + "/external/verify-email?token=" + token;

            // 이메일 템플릿 컨텍스트 설정
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationLink", verificationLink);

            // HTML 이메일 내용 생성
            String htmlContent = templateEngine.process("email/verification", context);

            // 메일 발송
            sendHtmlEmail(toEmail, subject, htmlContent);

            log.info("이메일 인증 메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 인증 메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    /**
     * HTML 이메일 발송
     *
     * @param to 수신자
     * @param subject 제목
     * @param htmlContent HTML 내용
     * @throws MessagingException 메일 발송 예외
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);  // true = HTML

        mailSender.send(message);
    }

    /**
     * 비밀번호 재설정 이메일 발송
     *
     * @param toEmail 수신자 이메일
     * @param name 수신자 이름
     * @param resetToken 재설정 토큰
     */
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        try {
            String subject = "[푸름대학교 SCMS] 비밀번호 재설정 안내";

            // 재설정 링크 생성
            String resetLink = serverDomain + "/external/reset-password?token=" + resetToken;

            // 이메일 템플릿 컨텍스트 설정
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            // HTML 이메일 내용 생성
            String htmlContent = templateEngine.process("email/password-reset", context);

            // 메일 발송
            sendHtmlEmail(toEmail, subject, htmlContent);

            log.info("비밀번호 재설정 메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }
}
