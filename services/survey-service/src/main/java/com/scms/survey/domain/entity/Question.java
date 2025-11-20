package com.scms.survey.domain.entity;

import com.scms.survey.domain.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 설문 질문 엔티티
 *
 * 설문의 개별 질문 정보를 저장합니다.
 */
@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    /**
     * 설문 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    /**
     * 질문 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    /**
     * 질문 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 질문 설명 (선택사항)
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 순서
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * 필수 응답 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean required = false;

    /**
     * 선택지 목록 (객관식인 경우)
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();

    /**
     * 최소값 (평점, 척도 타입인 경우)
     */
    private Integer minValue;

    /**
     * 최대값 (평점, 척도 타입인 경우)
     */
    private Integer maxValue;

    /**
     * 최소 레이블 (척도 타입인 경우, 예: "매우 불만족")
     */
    @Column(length = 100)
    private String minLabel;

    /**
     * 최대 레이블 (척도 타입인 경우, 예: "매우 만족")
     */
    @Column(length = 100)
    private String maxLabel;

    /**
     * 최대 선택 개수 (복수 선택인 경우)
     */
    private Integer maxSelections;

    /**
     * 최대 텍스트 길이 (단답형, 서술형인 경우)
     */
    private Integer maxLength;

    /**
     * 파일 업로드 허용 확장자 (파일 첨부 타입인 경우)
     */
    @Column(length = 200)
    private String allowedFileExtensions;

    /**
     * 파일 최대 크기 (bytes)
     */
    private Long maxFileSize;

    /**
     * 생성일시
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (Soft Delete)
     */
    private LocalDateTime deletedAt;

    // ===== 연관관계 편의 메서드 =====

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 질문 내용 수정
     */
    public void update(String content, String description, Boolean required, Integer displayOrder) {
        this.content = content;
        this.description = description;
        this.required = required;
        this.displayOrder = displayOrder;
    }

    /**
     * 선택지 추가
     */
    public void addOption(QuestionOption option) {
        this.options.add(option);
        option.setQuestion(this);
    }

    /**
     * 선택지 제거
     */
    public void removeOption(QuestionOption option) {
        this.options.remove(option);
        option.setQuestion(null);
    }

    /**
     * 순서 변경
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * 평점/척도 설정 업데이트
     */
    public void updateRatingScale(Integer minValue, Integer maxValue, String minLabel, String maxLabel) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minLabel = minLabel;
        this.maxLabel = maxLabel;
    }

    /**
     * 파일 업로드 설정 업데이트
     */
    public void updateFileUploadSettings(String allowedFileExtensions, Long maxFileSize) {
        this.allowedFileExtensions = allowedFileExtensions;
        this.maxFileSize = maxFileSize;
    }

    /**
     * 객관식 질문 여부
     */
    public boolean isMultipleChoice() {
        return type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE;
    }

    /**
     * 텍스트 응답 질문 여부
     */
    public boolean isTextAnswer() {
        return type == QuestionType.SHORT_ANSWER || type == QuestionType.LONG_ANSWER;
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
