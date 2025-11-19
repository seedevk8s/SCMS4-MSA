package com.scms.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그인 응답 DTO
 * - 로그인 성공 시 JWT 토큰과 사용자 정보 반환
 * - 내부 회원, 외부 회원 모두 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * 액세스 토큰 (JWT)
     */
    private String accessToken;

    /**
     * 리프레시 토큰 (JWT)
     */
    private String refreshToken;

    /**
     * 토큰 타입 (Bearer)
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 액세스 토큰 만료 시간 (초)
     */
    private Long expiresIn;

    /**
     * 사용자 ID
     */
    private Integer userId;

    /**
     * 로그인 ID (학번 또는 이메일)
     */
    private String loginId;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 사용자 역할
     */
    private String role;

    /**
     * 사용자 유형 (INTERNAL: 내부 회원, EXTERNAL: 외부 회원)
     */
    private String userType;

    /**
     * 로그인 시간
     */
    private LocalDateTime loginAt;

    /**
     * 내부 회원 로그인 응답 생성
     */
    public static LoginResponse ofInternal(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            UserResponse user
    ) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(user.getUserId())
                .loginId(String.valueOf(user.getStudentNum()))
                .name(user.getName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .userType("INTERNAL")
                .loginAt(LocalDateTime.now())
                .build();
    }

    /**
     * 외부 회원 로그인 응답 생성
     */
    public static LoginResponse ofExternal(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            ExternalUserResponse user
    ) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(user.getUserId())
                .loginId(user.getEmail())
                .name(user.getName())
                .role("EXTERNAL_USER")
                .userType("EXTERNAL")
                .loginAt(LocalDateTime.now())
                .build();
    }
}
