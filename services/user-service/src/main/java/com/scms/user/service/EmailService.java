package com.scms.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스
 *
 * 주요 기능:
 * - 비밀번호 재설정 이메일 발송
 * - 회원가입 환영 이메일 발송
 * - 이메일 인증 코드 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@scms.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 비밀번호 재설정 이메일 발송
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[SCMS] 비밀번호 재설정 요청");
            message.setText(
                    "안녕하세요,\n\n" +
                            "비밀번호 재설정을 요청하셨습니다.\n\n" +
                            "아래 링크를 클릭하여 비밀번호를 재설정해주세요:\n" +
                            resetLink + "\n\n" +
                            "이 링크는 1시간 동안 유효합니다.\n\n" +
                            "비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시해주세요.\n\n" +
                            "감사합니다.\n" +
                            "SCMS 팀"
            );

            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 성공: {}", toEmail);

        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", toEmail, e);
            // 실패해도 예외를 던지지 않음 (보안상 사용자에게 에러 노출 방지)
        }
    }

    /**
     * 회원가입 환영 이메일 발송
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[SCMS] 회원가입을 환영합니다!");
            message.setText(
                    "안녕하세요 " + userName + "님,\n\n" +
                            "SCMS(Student Career Management System)에 가입해주셔서 감사합니다.\n\n" +
                            "이제 다양한 비교과 프로그램에 참여하고, 포트폴리오를 관리하며,\n" +
                            "진로 상담을 받을 수 있습니다.\n\n" +
                            "로그인 페이지: " + frontendUrl + "/login\n\n" +
                            "궁금한 사항이 있으시면 언제든지 문의해주세요.\n\n" +
                            "감사합니다.\n" +
                            "SCMS 팀"
            );

            mailSender.send(message);
            log.info("환영 이메일 발송 성공: {}", toEmail);

        } catch (Exception e) {
            log.error("환영 이메일 발송 실패: {}", toEmail, e);
        }
    }

    /**
     * 이메일 인증 코드 발송 (외부 사용자용)
     */
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            String verificationLink = frontendUrl + "/verify-email?code=" + verificationCode;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[SCMS] 이메일 인증 요청");
            message.setText(
                    "안녕하세요,\n\n" +
                            "SCMS 회원가입을 위해 이메일 인증이 필요합니다.\n\n" +
                            "아래 링크를 클릭하여 이메일을 인증해주세요:\n" +
                            verificationLink + "\n\n" +
                            "또는 아래 인증 코드를 입력해주세요:\n" +
                            verificationCode + "\n\n" +
                            "이 코드는 24시간 동안 유효합니다.\n\n" +
                            "감사합니다.\n" +
                            "SCMS 팀"
            );

            mailSender.send(message);
            log.info("이메일 인증 코드 발송 성공: {}", toEmail);

        } catch (Exception e) {
            log.error("이메일 인증 코드 발송 실패: {}", toEmail, e);
        }
    }

    /**
     * 프로그램 신청 승인 알림 이메일 (Notification Service와 연동 시 사용)
     */
    public void sendProgramApprovalEmail(String toEmail, String userName, String programName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[SCMS] 프로그램 신청이 승인되었습니다");
            message.setText(
                    "안녕하세요 " + userName + "님,\n\n" +
                            "신청하신 프로그램이 승인되었습니다.\n\n" +
                            "프로그램명: " + programName + "\n\n" +
                            "자세한 내용은 SCMS 시스템에서 확인하실 수 있습니다.\n" +
                            frontendUrl + "/my-programs\n\n" +
                            "감사합니다.\n" +
                            "SCMS 팀"
            );

            mailSender.send(message);
            log.info("프로그램 승인 이메일 발송 성공: {}", toEmail);

        } catch (Exception e) {
            log.error("프로그램 승인 이메일 발송 실패: {}", toEmail, e);
        }
    }

    /**
     * 상담 예약 확인 이메일
     */
    public void sendConsultationConfirmationEmail(
            String toEmail,
            String userName,
            String counselorName,
            String consultationDate
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[SCMS] 상담 예약이 확정되었습니다");
            message.setText(
                    "안녕하세요 " + userName + "님,\n\n" +
                            "상담 예약이 확정되었습니다.\n\n" +
                            "상담사: " + counselorName + "\n" +
                            "상담 일시: " + consultationDate + "\n\n" +
                            "상담 전 준비사항이나 질문 사항은 SCMS 시스템을 통해 미리 전달해주시기 바랍니다.\n\n" +
                            "감사합니다.\n" +
                            "SCMS 팀"
            );

            mailSender.send(message);
            log.info("상담 예약 확인 이메일 발송 성공: {}", toEmail);

        } catch (Exception e) {
            log.error("상담 예약 확인 이메일 발송 실패: {}", toEmail, e);
        }
    }
}
