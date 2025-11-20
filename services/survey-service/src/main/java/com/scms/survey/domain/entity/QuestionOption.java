package com.scms.survey.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 질문 선택지 엔티티
 *
 * 객관식 질문의 선택지를 저장합니다.
 */
@Entity
@Table(name = "question_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    /**
     * 질문 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 선택지 내용
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 순서
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * 기타 의견 입력 허용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowOtherInput = false;

    /**
     * 선택 횟수 (통계용)
     */
    @Column(nullable = false)
    @Builder.Default
    private Long selectionCount = 0L;

    /**
     * 생성일시
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 삭제일시 (Soft Delete)
     */
    private LocalDateTime deletedAt;

    // ===== 연관관계 편의 메서드 =====

    public void setQuestion(Question question) {
        this.question = question;
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 선택지 내용 수정
     */
    public void update(String content, Integer displayOrder, Boolean allowOtherInput) {
        this.content = content;
        this.displayOrder = displayOrder;
        this.allowOtherInput = allowOtherInput;
    }

    /**
     * 선택 횟수 증가
     */
    public void incrementSelectionCount() {
        this.selectionCount++;
    }

    /**
     * 순서 변경
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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
