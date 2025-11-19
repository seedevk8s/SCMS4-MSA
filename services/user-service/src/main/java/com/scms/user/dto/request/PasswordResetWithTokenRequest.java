package com.scms.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰으로 비밀번호 재설정 요청 DTO
 * - 재설정 토큰을 받은 후 새 비밀번호 설정 시 사용
 * - 내부 회원, 외부 회원 모두 사용 가능
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetWithTokenRequest {

    /**
     * 비밀번호 재설정 토큰
     */
    @NotBlank(message = "재설정 토큰은 필수입니다")
    @Size(max = 255, message = "토큰은 255자 이하여야 합니다")
    private String token;

    /**
     * 새 비밀번호
     */
    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String newPassword;

    /**
     * 새 비밀번호 확인
     */
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String newPasswordConfirm;

    /**
     * 새 비밀번호 일치 여부 확인
     */
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
