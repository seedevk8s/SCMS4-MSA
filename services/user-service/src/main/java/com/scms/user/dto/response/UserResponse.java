package com.scms.user.dto.response;

import com.scms.user.domain.entity.User;
import com.scms.user.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 * - 내부 회원(학생, 상담사, 관리자) 정보 응답
 * - 민감 정보(비밀번호) 제외
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * 사용자 ID
     */
    private Integer userId;

    /**
     * 학번 (로그인 ID)
     */
    private Integer studentNum;

    /**
     * 이름
     */
    private String name;

    /**
     * 이메일
     */
    private String email;

    /**
     * 전화번호
     */
    private String phone;

    /**
     * 생년월일
     */
    private LocalDate birthDate;

    /**
     * 학과/부서
     */
    private String department;

    /**
     * 학년
     */
    private Integer grade;

    /**
     * 사용자 역할 (STUDENT, COUNSELOR, ADMIN)
     */
    private UserRole role;

    /**
     * 역할 설명
     */
    private String roleDescription;

    /**
     * 계정 잠금 여부
     */
    private Boolean locked;

    /**
     * 로그인 실패 횟수
     */
    private Integer failCnt;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 삭제일시
     */
    private LocalDateTime deletedAt;

    /**
     * Entity → DTO 변환
     */
    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .userId(user.getUserId())
                .studentNum(user.getStudentNum())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .department(user.getDepartment())
                .grade(user.getGrade())
                .role(user.getRole())
                .roleDescription(user.getRole() != null ? user.getRole().getDescription() : null)
                .locked(user.getLocked())
                .failCnt(user.getFailCnt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    /**
     * 활성 계정 여부
     */
    public boolean isActive() {
        return deletedAt == null && (locked == null || !locked);
    }

    /**
     * 삭제된 계정 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
