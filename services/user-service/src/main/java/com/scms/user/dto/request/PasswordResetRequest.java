package com.scms.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 비밀번호 재설정 요청 DTO (학번 + 이름 + 생년월일)
 * - 내부 회원(학생, 상담사, 관리자)의 비밀번호 재설정 시 사용
 * - 학번, 이름, 생년월일로 본인 확인
 * - 확인 후 재설정 토큰 발급
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequest {

    /**
     * 학번 (로그인 ID)
     */
    @NotNull(message = "학번은 필수입니다")
    @Min(value = 1, message = "학번은 1 이상이어야 합니다")
    private Integer studentNum;

    /**
     * 이름
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    private String name;

    /**
     * 생년월일
     */
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;
}
