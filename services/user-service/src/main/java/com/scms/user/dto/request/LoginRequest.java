package com.scms.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 * - 내부 회원: 학번 + 비밀번호
 * - 외부 회원: 이메일 + 비밀번호
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * 로그인 ID
     * - 내부 회원: 학번 (숫자)
     * - 외부 회원: 이메일
     */
    @NotBlank(message = "로그인 ID는 필수입니다")
    @Size(max = 100, message = "로그인 ID는 100자 이하여야 합니다")
    private String loginId;

    /**
     * 비밀번호
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, max = 100, message = "비밀번호는 4자 이상 100자 이하여야 합니다")
    private String password;

    /**
     * 로그인 유지 여부
     */
    @Builder.Default
    private Boolean rememberMe = false;
}
