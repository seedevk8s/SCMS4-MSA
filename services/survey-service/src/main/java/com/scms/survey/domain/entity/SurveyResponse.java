package com.scms.survey.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 설문 응답 엔티티
 *
 * 사용자의 설문 응답을 저장합니다.
 */
@Entity
@Table(name = "survey_responses",
        indexes = {
                @Index(name = "idx_survey_user", columnList = "survey_id,user_id"),
                @Index(name = "idx_question_user", columnList = "question_id,user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Long responseId;

    /**
     * 설문 ID
     */
    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    /**
     * 질문 ID
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 응답자 ID (익명이면 null)
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 선택한 옵션 ID (객관식인 경우)
     * 복수 선택인 경우 쉼표로 구분된 ID 목록
     */
    @Column(name = "selected_option_ids", length = 500)
    private String selectedOptionIds;

    /**
     * 텍스트 응답 (주관식, 기타 의견 등)
     */
    @Column(columnDefinition = "TEXT")
    private String textAnswer;

    /**
     * 숫자 응답 (평점, 척도 등)
     */
    private Integer numberAnswer;

    /**
     * 날짜 응답
     */
    private LocalDateTime dateAnswer;

    /**
     * 파일 URL (파일 첨부인 경우)
     */
    @Column(length = 500)
    private String fileUrl;

    /**
     * 파일명 (파일 첨부인 경우)
     */
    @Column(length = 255)
    private String fileName;

    /**
     * 세션 ID (익명 응답 추적용)
     */
    @Column(length = 100)
    private String sessionId;

    /**
     * IP 주소 (통계용)
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * 생성일시 (최초 응답 시간)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시 (응답 수정 시간)
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (Soft Delete)
     */
    private LocalDateTime deletedAt;

    // ===== 비즈니스 메서드 =====

    /**
     * 텍스트 응답 업데이트
     */
    public void updateTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    /**
     * 선택지 응답 업데이트
     */
    public void updateSelectedOptions(String selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }

    /**
     * 숫자 응답 업데이트
     */
    public void updateNumberAnswer(Integer numberAnswer) {
        this.numberAnswer = numberAnswer;
    }

    /**
     * 날짜 응답 업데이트
     */
    public void updateDateAnswer(LocalDateTime dateAnswer) {
        this.dateAnswer = dateAnswer;
    }

    /**
     * 파일 응답 업데이트
     */
    public void updateFileAnswer(String fileUrl, String fileName) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }

    /**
     * Soft Delete
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 복원
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
