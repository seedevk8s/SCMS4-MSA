package com.scms.user.dto.response;

import com.scms.user.domain.entity.ExternalUser;
import com.scms.user.domain.enums.AccountStatus;
import com.scms.user.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 외부 회원 정보 응답 DTO
 * - 외부 회원(이메일 로그인) 정보 응답
 * - 민감 정보(비밀번호) 제외
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalUserResponse {

    /**
     * 사용자 ID
     */
    private Integer userId;

    /**
     * 이메일 (로그인 ID)
     */
    private String email;

    /**
     * 로그인 제공자 (LOCAL, GOOGLE, NAVER, KAKAO 등)
     */
    private String provider;

    /**
     * 제공자 설명
     */
    private String providerDescription;

    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;

    /**
     * 이름
     */
    private String name;

    /**
     * 전화번호
     */
    private String phone;

    /**
     * 생년월일
     */
    private LocalDate birthDate;

    /**
     * 주소
     */
    private String address;

    /**
     * 성별
     */
    private Gender gender;

    /**
     * 성별 설명
     */
    private String genderDescription;

    /**
     * 계정 상태 (ACTIVE, INACTIVE, SUSPENDED)
     */
    private AccountStatus status;

    /**
     * 계정 상태 설명
     */
    private String statusDescription;

    /**
     * 계정 잠금 여부
     */
    private Boolean locked;

    /**
     * 로그인 실패 횟수
     */
    private Integer failCnt;

    /**
     * 이메일 인증 여부
     */
    private Boolean emailVerified;

    /**
     * 이메일 인증 완료 일시
     */
    private LocalDateTime emailVerifiedAt;

    /**
     * 이용약관 동의 여부
     */
    private Boolean agreeTerms;

    /**
     * 개인정보처리방침 동의 여부
     */
    private Boolean agreePrivacy;

    /**
     * 마케팅 정보 수신 동의 여부
     */
    private Boolean agreeMarketing;

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
     * 마지막 로그인 일시
     */
    private LocalDateTime lastLoginAt;

    /**
     * Entity → DTO 변환
     */
    public static ExternalUserResponse from(ExternalUser user) {
        if (user == null) {
            return null;
        }

        return ExternalUserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .provider(user.getProvider())
                .providerDescription(getProviderDescription(user.getProvider()))
                .profileImageUrl(user.getProfileImageUrl())
                .name(user.getName())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .address(user.getAddress())
                .gender(user.getGender())
                .genderDescription(user.getGender() != null ? user.getGender().getDescription() : null)
                .status(user.getStatus())
                .statusDescription(user.getStatus() != null ? user.getStatus().getDescription() : null)
                .locked(user.getLocked())
                .failCnt(user.getFailCnt())
                .emailVerified(user.getEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .agreeTerms(user.getAgreeTerms())
                .agreePrivacy(user.getAgreePrivacy())
                .agreeMarketing(user.getAgreeMarketing())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * 로그인 제공자 설명 반환
     */
    private static String getProviderDescription(String provider) {
        if (provider == null) {
            return null;
        }
        return switch (provider.toUpperCase()) {
            case "LOCAL" -> "일반 회원가입";
            case "GOOGLE" -> "구글";
            case "NAVER" -> "네이버";
            case "KAKAO" -> "카카오";
            default -> provider;
        };
    }

    /**
     * 활성 계정 여부
     */
    public boolean isActive() {
        return deletedAt == null
                && (locked == null || !locked)
                && AccountStatus.ACTIVE.equals(status);
    }

    /**
     * 삭제된 계정 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 소셜 로그인 사용자 여부
     */
    public boolean isSocialUser() {
        return !"LOCAL".equals(provider);
    }
}
