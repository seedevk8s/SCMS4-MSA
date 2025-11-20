package com.scms.survey.controller;

import com.scms.common.dto.ApiResponse;
import com.scms.survey.domain.enums.SurveyStatus;
import com.scms.survey.dto.request.QuestionCreateRequest;
import com.scms.survey.dto.request.SurveyCreateRequest;
import com.scms.survey.dto.request.SurveyResponseSubmitRequest;
import com.scms.survey.dto.response.SurveyResponse;
import com.scms.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 설문 컨트롤러
 *
 * 설문조사 관련 REST API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * 설문 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SurveyResponse> createSurvey(
            @Valid @RequestBody SurveyCreateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        SurveyResponse response = surveyService.createSurvey(request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 질문 추가
     */
    @PostMapping("/{surveyId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addQuestion(
            @PathVariable Long surveyId,
            @Valid @RequestBody QuestionCreateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        surveyService.addQuestion(surveyId, request, userId);
        return ApiResponse.success();
    }

    /**
     * 설문 응답 제출
     */
    @PostMapping("/{surveyId}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> submitResponse(
            @PathVariable Long surveyId,
            @Valid @RequestBody SurveyResponseSubmitRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        surveyService.submitResponse(surveyId, request, userId);
        return ApiResponse.success();
    }

    /**
     * 설문 목록 조회
     */
    @GetMapping
    public ApiResponse<List<SurveyResponse>> getSurveys() {
        List<SurveyResponse> responses = surveyService.getSurveys();
        return ApiResponse.success(responses);
    }

    /**
     * 응답 가능한 설문 목록
     */
    @GetMapping("/available")
    public ApiResponse<List<SurveyResponse>> getAvailableSurveys() {
        List<SurveyResponse> responses = surveyService.getAvailableSurveys();
        return ApiResponse.success(responses);
    }

    /**
     * 설문 상세 조회
     */
    @GetMapping("/{surveyId}")
    public ApiResponse<SurveyResponse> getSurvey(@PathVariable Long surveyId) {
        SurveyResponse response = surveyService.getSurvey(surveyId);
        return ApiResponse.success(response);
    }

    /**
     * 설문 상태 변경
     */
    @PatchMapping("/{surveyId}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long surveyId,
            @RequestParam SurveyStatus status,
            @RequestHeader("X-User-Id") Long userId
    ) {
        surveyService.updateStatus(surveyId, status, userId);
        return ApiResponse.success();
    }

    /**
     * 설문 삭제
     */
    @DeleteMapping("/{surveyId}")
    public ApiResponse<Void> deleteSurvey(
            @PathVariable Long surveyId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        surveyService.deleteSurvey(surveyId, userId);
        return ApiResponse.success();
    }
}
