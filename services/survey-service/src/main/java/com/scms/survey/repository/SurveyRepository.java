package com.scms.survey.repository;

import com.scms.survey.domain.entity.Survey;
import com.scms.survey.domain.enums.SurveyStatus;
import com.scms.survey.domain.enums.SurveyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 설문 Repository
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /**
     * 모든 설문 목록 (최신순)
     */
    List<Survey> findByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * 상태별 설문 목록
     */
    List<Survey> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(SurveyStatus status);

    /**
     * 타입별 설문 목록
     */
    List<Survey> findByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(SurveyType type);

    /**
     * 생성자별 설문 목록
     */
    List<Survey> findByCreatedByAndDeletedAtIsNullOrderByCreatedAtDesc(Long createdBy);

    /**
     * 설문 상세 조회
     */
    Optional<Survey> findBySurveyIdAndDeletedAtIsNull(Long surveyId);

    /**
     * 응답 가능한 설문 목록 (공개 + 응답 기간 내)
     */
    @Query("SELECT s FROM Survey s WHERE s.status = 'PUBLISHED' " +
            "AND (s.startDate IS NULL OR s.startDate <= :now) " +
            "AND (s.endDate IS NULL OR s.endDate >= :now) " +
            "AND (s.maxResponses IS NULL OR s.responseCount < s.maxResponses) " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.createdAt DESC")
    List<Survey> findAvailableSurveys(@Param("now") LocalDateTime now);

    /**
     * 마감 임박 설문 (N일 이내 마감)
     */
    @Query("SELECT s FROM Survey s WHERE s.status = 'PUBLISHED' " +
            "AND s.endDate IS NOT NULL " +
            "AND s.endDate BETWEEN :now AND :deadline " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.endDate ASC")
    List<Survey> findClosingSoonSurveys(@Param("now") LocalDateTime now,
                                        @Param("deadline") LocalDateTime deadline);

    /**
     * 제목으로 검색
     */
    @Query("SELECT s FROM Survey s WHERE s.title LIKE %:keyword% " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.createdAt DESC")
    List<Survey> searchByTitle(@Param("keyword") String keyword);

    /**
     * 응답 수 증가
     */
    @Modifying
    @Query("UPDATE Survey s SET s.responseCount = s.responseCount + 1 WHERE s.surveyId = :surveyId")
    void incrementResponseCount(@Param("surveyId") Long surveyId);

    /**
     * 설문 개수 (상태별)
     */
    long countByStatusAndDeletedAtIsNull(SurveyStatus status);

    /**
     * 설문 개수 (생성자별)
     */
    long countByCreatedByAndDeletedAtIsNull(Long createdBy);
}
