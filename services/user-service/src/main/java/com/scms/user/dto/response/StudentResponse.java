package com.scms.user.dto.response;

import com.scms.user.domain.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학생 정보 응답 DTO
 * - 레거시 학생 테이블(students) 정보 응답
 * - 학사 정보 시스템 연동용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    /**
     * 학생 테이블 ID
     */
    private Long id;

    /**
     * 학번
     */
    private String studentId;

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
     * 학과
     */
    private String department;

    /**
     * 학년
     */
    private String grade;

    /**
     * 재학 상태 (재학, 휴학, 졸업 등)
     */
    private String status;

    /**
     * 재학 상태 설명
     */
    private String statusDescription;

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
    public static StudentResponse from(Student student) {
        if (student == null) {
            return null;
        }

        return StudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .department(student.getDepartment())
                .grade(student.getGrade())
                .status(student.getStatus())
                .statusDescription(getStatusDescription(student.getStatus()))
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .deletedAt(student.getDeletedAt())
                .build();
    }

    /**
     * 재학 상태 설명 반환
     */
    private static String getStatusDescription(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "재학" -> "재학중";
            case "휴학" -> "휴학중";
            case "졸업" -> "졸업";
            case "자퇴" -> "자퇴";
            case "제적" -> "제적";
            default -> status;
        };
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 재학중 여부
     */
    public boolean isEnrolled() {
        return "재학".equals(status) && !isDeleted();
    }

    /**
     * 졸업 여부
     */
    public boolean isGraduated() {
        return "졸업".equals(status);
    }
}
