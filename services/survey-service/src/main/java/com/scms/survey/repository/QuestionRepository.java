package com.scms.survey.repository;

import com.scms.survey.domain.entity.Question;
import com.scms.survey.domain.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 질문 Repository
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 설문별 질문 목록 (순서대로)
     */
    List<Question> findBySurvey_SurveyIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long surveyId);

    /**
     * 설문 + 타입별 질문 목록
     */
    List<Question> findBySurvey_SurveyIdAndTypeAndDeletedAtIsNullOrderByDisplayOrderAsc(
            Long surveyId,
            QuestionType type
    );

    /**
     * 필수 질문 목록
     */
    List<Question> findBySurvey_SurveyIdAndRequiredTrueAndDeletedAtIsNullOrderByDisplayOrderAsc(Long surveyId);

    /**
     * 질문 상세 조회
     */
    Optional<Question> findByQuestionIdAndDeletedAtIsNull(Long questionId);

    /**
     * 설문별 질문 개수
     */
    long countBySurvey_SurveyIdAndDeletedAtIsNull(Long surveyId);
}
