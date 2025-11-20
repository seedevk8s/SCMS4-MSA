package com.scms.survey.domain.entity;

import com.scms.survey.domain.enums.SurveyStatus;
import com.scms.survey.domain.enums.SurveyType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 설문 엔티티
 *
 * 설문조사 기본 정보를 저장합니다.
 */
@Entity
@Table(name = "surveys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long surveyId;

    /**
     * 설문 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 설문 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 설문 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SurveyType type;

    /**
     * 설문 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SurveyStatus status = SurveyStatus.DRAFT;

    /**
     * 생성자 ID (관리자/상담사)
     */
    @Column(nullable = false)
    private Long createdBy;

    /**
     * 대상 사용자 ID 목록 (JSON 배열 또는 쉼표 구분)
     * null이면 전체 대상
     */
    @Column(columnDefinition = "TEXT")
    private String targetUserIds;

    /**
     * 대상 그룹 (예: 학과, 학년 등)
     */
    @Column(length = 100)
    private String targetGroup;

    /**
     * 익명 응답 허용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean anonymous = false;

    /**
     * 중복 응답 허용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowMultipleResponses = false;

    /**
     * 응답 수정 허용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowEdit = true;

    /**
     * 결과 공개 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean showResults = false;

    /**
     * 시작일시
     */
    private LocalDateTime startDate;

    /**
     * 종료일시
     */
    private LocalDateTime endDate;

    /**
     * 응답 수
     */
    @Column(nullable = false)
    @Builder.Default
    private Long responseCount = 0L;

    /**
     * 최대 응답 수 (제한 없으면 null)
     */
    private Long maxResponses;

    /**
     * 질문 목록
     */
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

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

    // ===== 비즈니스 메서드 =====

    /**
     * 설문 정보 수정
     */
    public void update(String title, String description, SurveyType type,
                      Boolean anonymous, Boolean allowMultipleResponses, Boolean allowEdit,
                      Boolean showResults, LocalDateTime startDate, LocalDateTime endDate,
                      String targetGroup, Long maxResponses) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.anonymous = anonymous;
        this.allowMultipleResponses = allowMultipleResponses;
        this.allowEdit = allowEdit;
        this.showResults = showResults;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetGroup = targetGroup;
        this.maxResponses = maxResponses;
    }

    /**
     * 상태 변경
     */
    public void updateStatus(SurveyStatus status) {
        this.status = status;
    }

    /**
     * 질문 추가
     */
    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setSurvey(this);
    }

    /**
     * 질문 제거
     */
    public void removeQuestion(Question question) {
        this.questions.remove(question);
        question.setSurvey(null);
    }

    /**
     * 응답 수 증가
     */
    public void incrementResponseCount() {
        this.responseCount++;
    }

    /**
     * 응답 가능 여부 확인
     */
    public boolean isAvailableForResponse() {
        if (this.status != SurveyStatus.PUBLISHED) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (this.startDate != null && now.isBefore(this.startDate)) {
            return false;
        }

        if (this.endDate != null && now.isAfter(this.endDate)) {
            return false;
        }

        if (this.maxResponses != null && this.responseCount >= this.maxResponses) {
            return false;
        }

        return true;
    }

    /**
     * 응답 기간 확인
     */
    public boolean isWithinResponsePeriod() {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = this.startDate == null || now.isAfter(this.startDate);
        boolean beforeEnd = this.endDate == null || now.isBefore(this.endDate);
        return afterStart && beforeEnd;
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
