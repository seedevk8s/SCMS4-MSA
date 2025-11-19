package com.scms.user.dto.request;

import com.scms.user.domain.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자 생성 요청 DTO
 * - 내부 회원(학생, 상담사, 관리자) 생성 시 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    /**
     * 학번 (로그인 ID로 사용)
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
     * 이메일
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;

    /**
     * 전화번호
     */
    @Pattern(regexp = "^[0-9-]+$", message = "전화번호는 숫자와 하이픈(-)만 입력 가능합니다")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phone;

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
     * 생년월일
     */
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;

    /**
     * 학과/부서
     */
    @Size(max = 100, message = "학과/부서는 100자 이하여야 합니다")
    private String department;

    /**
     * 학년
     */
    @Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @Max(value = 6, message = "학년은 6 이하여야 합니다")
    private Integer grade;

    /**
     * 사용자 역할 (STUDENT, COUNSELOR, ADMIN)
     */
    @NotNull(message = "역할은 필수입니다")
    private UserRole role;

    /**
     * 비밀번호 일치 여부 확인
     */
    public boolean isPasswordMatch() {
        return password != null && password.equals(passwordConfirm);
    }
}
