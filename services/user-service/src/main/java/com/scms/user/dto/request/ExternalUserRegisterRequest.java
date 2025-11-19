package com.scms.user.dto.request;

import com.scms.user.domain.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 외부 회원 가입 요청 DTO
 * - 이메일로 로그인하는 외부 사용자 회원가입 시 사용
 * - 일반 회원가입 (소셜 로그인은 별도 처리)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalUserRegisterRequest {

    /**
     * 이메일 (로그인 ID)
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;

    /**
     * 비밀번호
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String password;

    /**
     * 비밀번호 확인
     */
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String passwordConfirm;

    /**
     * 이름
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    private String name;

    /**
     * 전화번호
     */
    @Pattern(regexp = "^[0-9-]+$", message = "전화번호는 숫자와 하이픈(-)만 입력 가능합니다")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phone;

    /**
     * 생년월일
     */
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;

    /**
     * 주소
     */
    @Size(max = 200, message = "주소는 200자 이하여야 합니다")
    private String address;

    /**
     * 성별
     */
    private Gender gender;

    /**
     * 이용약관 동의 여부
     */
    @NotNull(message = "이용약관 동의는 필수입니다")
    @AssertTrue(message = "이용약관에 동의해야 합니다")
    private Boolean agreeTerms;

    /**
     * 개인정보처리방침 동의 여부
     */
    @NotNull(message = "개인정보처리방침 동의는 필수입니다")
    @AssertTrue(message = "개인정보처리방침에 동의해야 합니다")
    private Boolean agreePrivacy;

    /**
     * 마케팅 정보 수신 동의 여부 (선택)
     */
    @Builder.Default
    private Boolean agreeMarketing = false;

    /**
     * 비밀번호 일치 여부 확인
     */
    public boolean isPasswordMatch() {
        return password != null && password.equals(passwordConfirm);
    }
}
