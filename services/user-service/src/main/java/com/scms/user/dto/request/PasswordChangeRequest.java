package com.scms.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO
 * - 로그인한 사용자가 자신의 비밀번호를 변경할 때 사용
 * - 현재 비밀번호 확인 필요
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeRequest {

    /**
     * 현재 비밀번호
     */
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

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
    public boolean isNewPasswordMatch() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }

    /**
     * 현재 비밀번호와 새 비밀번호가 같은지 확인
     */
    public boolean isSamePassword() {
        return currentPassword != null && currentPassword.equals(newPassword);
    }
}
