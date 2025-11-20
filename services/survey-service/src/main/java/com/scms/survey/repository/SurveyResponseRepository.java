package com.scms.survey.repository;

import com.scms.survey.domain.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 설문 응답 Repository
 */
@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    /**
     * 설문별 응답 목록
     */
    List<SurveyResponse> findBySurveyIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long surveyId);

    /**
     * 사용자별 응답 목록 (특정 설문)
     */
    List<SurveyResponse> findBySurveyIdAndUserIdAndDeletedAtIsNullOrderByQuestionIdAsc(
            Long surveyId,
            Long userId
    );

    /**
     * 질문별 응답 목록
     */
    List<SurveyResponse> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long questionId);

    /**
     * 사용자의 특정 질문 응답 조회
     */
    Optional<SurveyResponse> findBySurveyIdAndQuestionIdAndUserIdAndDeletedAtIsNull(
            Long surveyId,
            Long questionId,
            Long userId
    );

    /**
     * 세션 ID로 응답 조회 (익명 응답)
     */
    List<SurveyResponse> findBySurveyIdAndSessionIdAndDeletedAtIsNullOrderByQuestionIdAsc(
            Long surveyId,
            String sessionId
    );

    /**
     * 설문 응답 여부 확인
     */
    boolean existsBySurveyIdAndUserIdAndDeletedAtIsNull(Long surveyId, Long userId);

    /**
     * 세션 ID로 응답 여부 확인 (익명)
     */
    boolean existsBySurveyIdAndSessionIdAndDeletedAtIsNull(Long surveyId, String sessionId);

    /**
     * 설문별 응답 수
     */
    long countBySurveyIdAndDeletedAtIsNull(Long surveyId);

    /**
     * 질문별 응답 수
     */
    long countByQuestionIdAndDeletedAtIsNull(Long questionId);

    /**
     * 특정 옵션 선택 횟수
     */
    @Query("SELECT COUNT(r) FROM SurveyResponse r WHERE r.selectedOptionIds LIKE %:optionId% " +
            "AND r.deletedAt IS NULL")
    long countBySelectedOption(@Param("optionId") String optionId);

    /**
     * 사용자별 전체 응답 목록
     */
    List<SurveyResponse> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
}
