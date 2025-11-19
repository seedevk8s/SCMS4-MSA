package com.scms.user.dto.response;

import com.scms.user.domain.entity.Counselor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담사 정보 응답 DTO
 * - 상담사 전용 정보 응답
 * - 기본 사용자 정보 + 상담사 전문 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselorResponse {

    /**
     * 상담사 ID (User ID와 동일)
     */
    private Integer counselorId;

    /**
     * 기본 사용자 정보
     */
    private UserResponse user;

    /**
     * 전문 분야
     */
    private String specialty;

    /**
     * 상담사 소개
     */
    private String introduction;

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
    public static CounselorResponse from(Counselor counselor) {
        if (counselor == null) {
            return null;
        }

        return CounselorResponse.builder()
                .counselorId(counselor.getCounselorId())
                .user(UserResponse.from(counselor.getUser()))
                .specialty(counselor.getSpecialty())
                .introduction(counselor.getIntroduction())
                .createdAt(counselor.getCreatedAt())
                .updatedAt(counselor.getUpdatedAt())
                .deletedAt(counselor.getDeletedAt())
                .build();
    }

    /**
     * 기본 정보만 포함한 간단한 응답 생성 (User 정보 제외)
     */
    public static CounselorResponse fromSimple(Counselor counselor) {
        if (counselor == null) {
            return null;
        }

        return CounselorResponse.builder()
                .counselorId(counselor.getCounselorId())
                .specialty(counselor.getSpecialty())
                .introduction(counselor.getIntroduction())
                .createdAt(counselor.getCreatedAt())
                .updatedAt(counselor.getUpdatedAt())
                .deletedAt(counselor.getDeletedAt())
                .build();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return deletedAt != null || (user != null && user.isDeleted());
    }

    /**
     * 활성 상담사 여부
     */
    public boolean isActive() {
        return !isDeleted() && user != null && user.isActive();
    }
}
