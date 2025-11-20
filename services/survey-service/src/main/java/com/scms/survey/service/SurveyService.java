package com.scms.survey.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.survey.domain.entity.*;
import com.scms.survey.domain.enums.SurveyStatus;
import com.scms.survey.dto.request.QuestionCreateRequest;
import com.scms.survey.dto.request.SurveyCreateRequest;
import com.scms.survey.dto.request.SurveyResponseSubmitRequest;
import com.scms.survey.dto.response.SurveyResponse;
import com.scms.survey.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 설문 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    /**
     * 설문 생성
     */
    @Transactional
    public SurveyResponse createSurvey(SurveyCreateRequest request, Long createdBy) {
        Survey survey = Survey.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .status(SurveyStatus.DRAFT)
                .createdBy(createdBy)
                .anonymous(request.getAnonymous())
                .allowMultipleResponses(request.getAllowMultipleResponses())
                .allowEdit(request.getAllowEdit())
                .showResults(request.getShowResults())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .targetGroup(request.getTargetGroup())
                .maxResponses(request.getMaxResponses())
                .build();

        Survey saved = surveyRepository.save(survey);
        log.info("설문 생성: surveyId={}, createdBy={}", saved.getSurveyId(), createdBy);

        return SurveyResponse.from(saved);
    }

    /**
     * 질문 추가
     */
    @Transactional
    public void addQuestion(Long surveyId, QuestionCreateRequest request, Long userId) {
        Survey survey = getSurveyEntity(surveyId);

        // 권한 확인
        if (!survey.getCreatedBy().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "설문 생성자만 질문을 추가할 수 있습니다.");
        }

        Question question = Question.builder()
                .survey(survey)
                .type(request.getType())
                .content(request.getContent())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .required(request.getRequired())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .minLabel(request.getMinLabel())
                .maxLabel(request.getMaxLabel())
                .maxSelections(request.getMaxSelections())
                .maxLength(request.getMaxLength())
                .allowedFileExtensions(request.getAllowedFileExtensions())
                .maxFileSize(request.getMaxFileSize())
                .build();

        // 선택지 추가
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (QuestionCreateRequest.OptionCreateRequest optionReq : request.getOptions()) {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .content(optionReq.getContent())
                        .displayOrder(optionReq.getDisplayOrder())
                        .allowOtherInput(optionReq.getAllowOtherInput())
                        .build();
                question.addOption(option);
            }
        }

        questionRepository.save(question);
        log.info("질문 추가: surveyId={}, questionId={}", surveyId, question.getQuestionId());
    }

    /**
     * 설문 응답 제출
     */
    @Transactional
    public void submitResponse(Long surveyId, SurveyResponseSubmitRequest request, Long userId) {
        Survey survey = getSurveyEntity(surveyId);

        // 응답 가능 여부 확인
        if (!survey.isAvailableForResponse()) {
            throw new ApiException(ErrorCode.SURVEY_CLOSED, "현재 응답할 수 없는 설문입니다.");
        }

        // 중복 응답 확인
        if (!survey.getAllowMultipleResponses()) {
            if (userId != null && surveyResponseRepository.existsBySurveyIdAndUserIdAndDeletedAtIsNull(surveyId, userId)) {
                throw new ApiException(ErrorCode.SURVEY_ALREADY_SUBMITTED, "이미 응답한 설문입니다.");
            }
            if (request.getSessionId() != null &&
                    surveyResponseRepository.existsBySurveyIdAndSessionIdAndDeletedAtIsNull(surveyId, request.getSessionId())) {
                throw new ApiException(ErrorCode.SURVEY_ALREADY_SUBMITTED, "이미 응답한 설문입니다.");
            }
        }

        // 응답 저장
        for (SurveyResponseSubmitRequest.Answer answer : request.getAnswers()) {
            String selectedOptionIds = null;
            if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty()) {
                selectedOptionIds = answer.getSelectedOptionIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

                // 선택 횟수 증가
                for (Long optionId : answer.getSelectedOptionIds()) {
                    questionOptionRepository.incrementSelectionCount(optionId);
                }
            }

            SurveyResponse response = com.scms.survey.domain.entity.SurveyResponse.builder()
                    .surveyId(surveyId)
                    .questionId(answer.getQuestionId())
                    .userId(survey.getAnonymous() ? null : userId)
                    .selectedOptionIds(selectedOptionIds)
                    .textAnswer(answer.getTextAnswer())
                    .numberAnswer(answer.getNumberAnswer())
                    .dateAnswer(answer.getDateAnswer())
                    .fileUrl(answer.getFileUrl())
                    .fileName(answer.getFileName())
                    .sessionId(request.getSessionId())
                    .build();

            surveyResponseRepository.save(response);
        }

        // 응답 수 증가
        surveyRepository.incrementResponseCount(surveyId);
        log.info("설문 응답 제출: surveyId={}, userId={}", surveyId, userId);
    }

    /**
     * 설문 목록 조회
     */
    public List<SurveyResponse> getSurveys() {
        return surveyRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(SurveyResponse::fromWithoutQuestions)
                .collect(Collectors.toList());
    }

    /**
     * 응답 가능한 설문 목록
     */
    public List<SurveyResponse> getAvailableSurveys() {
        return surveyRepository.findAvailableSurveys(LocalDateTime.now())
                .stream()
                .map(SurveyResponse::fromWithoutQuestions)
                .collect(Collectors.toList());
    }

    /**
     * 설문 상세 조회
     */
    public SurveyResponse getSurvey(Long surveyId) {
        Survey survey = getSurveyEntity(surveyId);
        return SurveyResponse.from(survey);
    }

    /**
     * 설문 상태 변경
     */
    @Transactional
    public void updateStatus(Long surveyId, SurveyStatus status, Long userId) {
        Survey survey = getSurveyEntity(surveyId);

        // 권한 확인
        if (!survey.getCreatedBy().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "설문 생성자만 상태를 변경할 수 있습니다.");
        }

        survey.updateStatus(status);
        log.info("설문 상태 변경: surveyId={}, status={}", surveyId, status);
    }

    /**
     * 설문 삭제
     */
    @Transactional
    public void deleteSurvey(Long surveyId, Long userId) {
        Survey survey = getSurveyEntity(surveyId);

        // 권한 확인
        if (!survey.getCreatedBy().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "설문 생성자만 삭제할 수 있습니다.");
        }

        survey.markAsDeleted();
        log.info("설문 삭제: surveyId={}", surveyId);
    }

    // ===== Private 메서드 =====

    private Survey getSurveyEntity(Long surveyId) {
        return surveyRepository.findBySurveyIdAndDeletedAtIsNull(surveyId)
                .orElseThrow(() -> new ApiException(ErrorCode.SURVEY_NOT_FOUND));
    }
}
