package com.scms.survey.repository;

import com.scms.survey.domain.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 질문 선택지 Repository
 */
@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    /**
     * 질문별 선택지 목록 (순서대로)
     */
    List<QuestionOption> findByQuestion_QuestionIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long questionId);

    /**
     * 선택지 상세 조회
     */
    Optional<QuestionOption> findByOptionIdAndDeletedAtIsNull(Long optionId);

    /**
     * 질문별 선택지 개수
     */
    long countByQuestion_QuestionIdAndDeletedAtIsNull(Long questionId);

    /**
     * 선택 횟수 증가
     */
    @Modifying
    @Query("UPDATE QuestionOption o SET o.selectionCount = o.selectionCount + 1 WHERE o.optionId = :optionId")
    void incrementSelectionCount(@Param("optionId") Long optionId);
}
