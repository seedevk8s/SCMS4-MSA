package com.scms.user.controller;

import com.scms.user.dto.request.*;
import com.scms.user.dto.response.LoginResponse;
import com.scms.user.dto.response.UserResponse;
import com.scms.user.service.AuthService;
import com.scms.user.service.ExternalUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 컨트롤러
 *
 * 엔드포인트:
 * - POST /api/auth/login - 로그인 (내부 사용자)
 * - POST /api/auth/login/external - 로그인 (외부 사용자)
 * - POST /api/auth/logout - 로그아웃
 * - POST /api/auth/refresh - 토큰 갱신
 * - POST /api/auth/password/change - 비밀번호 변경
 * - POST /api/auth/password/reset-request - 비밀번호 재설정 요청
 * - POST /api/auth/password/reset - 비밀번호 재설정
 * - POST /api/auth/register/external - 외부 사용자 회원가입
 * - POST /api/auth/verify-email - 이메일 인증
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ExternalUserService externalUserService;

    /**
     * 로그인 - 내부 사용자 (학생, 관리자)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.loginInternal(request, ipAddress, userAgent);
        log.info("로그인 성공: loginId={}, ip={}", request.getLoginId(), ipAddress);

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 - 외부 사용자
     */
    @PostMapping("/login/external")
    public ResponseEntity<LoginResponse> loginExternal(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.loginExternal(request, ipAddress, userAgent);
        log.info("외부 사용자 로그인 성공: email={}, ip={}", request.getLoginId(), ipAddress);

        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     * (클라이언트에서 토큰 삭제 처리, 서버는 로그만 기록)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("로그아웃 요청");
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    /**
     * 토큰 갱신 (Refresh Token 사용)
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @RequestBody Map<String, String> request
    ) {
        String refreshToken = request.get("refreshToken");
        LoginResponse response = authService.refreshToken(refreshToken);
        log.info("토큰 갱신 성공");

        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경 (로그인 상태)
     */
    @PostMapping("/password/change")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @RequestAttribute("userId") Long userId  // JWT 필터에서 주입
    ) {
        authService.changePassword(userId, request);
        log.info("비밀번호 변경 성공: userId={}", userId);

        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    /**
     * 비밀번호 재설정 요청 (이메일 발송)
     */
    @PostMapping("/password/reset-request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.requestPasswordReset(request);
        log.info("비밀번호 재설정 요청: email={}", request.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호 재설정 링크가 이메일로 발송되었습니다."
        ));
    }

    /**
     * 비밀번호 재설정 (토큰 사용)
     */
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        authService.resetPassword(token, request);
        log.info("비밀번호 재설정 성공: token={}", token);

        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 재설정되었습니다."));
    }

    /**
     * 외부 사용자 회원가입 (로컬)
     */
    @PostMapping("/register/external")
    public ResponseEntity<UserResponse> registerExternalUser(
            @Valid @RequestBody ExternalUserCreateRequest request
    ) {
        UserResponse response = externalUserService.registerExternalUser(request);
        log.info("외부 사용자 회원가입 성공: email={}", request.getEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 인증
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestParam String code
    ) {
        externalUserService.verifyEmail(code);
        log.info("이메일 인증 성공: code={}", code);

        return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
    }

    /**
     * 이메일 인증 코드 재발송
     */
    @PostMapping("/verify-email/resend")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @RequestParam String email
    ) {
        externalUserService.resendVerificationEmail(email);
        log.info("이메일 인증 코드 재발송: email={}", email);

        return ResponseEntity.ok(Map.of("message", "인증 코드가 재발송되었습니다."));
    }

    /**
     * 토큰 검증
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = authService.validateToken(token);

        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    // ==================== Helper Methods ====================

    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
