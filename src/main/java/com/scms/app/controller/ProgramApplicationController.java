package com.scms.app.controller;

import com.scms.app.dto.ProgramApplicationResponse;
import com.scms.app.model.ProgramApplication;
import com.scms.app.service.ProgramApplicationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 프로그램 신청 API Controller
 */
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@Slf4j
public class ProgramApplicationController {

    private final ProgramApplicationService applicationService;

    /**
     * 프로그램 신청
     */
    @PostMapping("/{programId}/apply")
    public ResponseEntity<?> applyProgram(
            @PathVariable Integer programId,
            HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            ProgramApplication application = applicationService.applyProgram(userId, programId);
            ProgramApplicationResponse response = ProgramApplicationResponse.from(application);

            log.info("프로그램 신청 성공: 사용자 ID {}, 프로그램 ID {}", userId, programId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로그램 신청이 완료되었습니다.",
                    "application", response
            ));

        } catch (IllegalArgumentException e) {
            log.error("프로그램 신청 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("프로그램 신청 실패 (상태 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("프로그램 신청 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 프로그램 신청 취소
     */
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<?> cancelApplication(
            @PathVariable Integer applicationId,
            HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            applicationService.cancelApplication(userId, applicationId);

            log.info("프로그램 신청 취소 성공: 사용자 ID {}, 신청 ID {}", userId, applicationId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로그램 신청이 취소되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.error("프로그램 신청 취소 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("프로그램 신청 취소 실패 (상태 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("프로그램 신청 취소 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 나의 신청 내역 조회
     */
    @GetMapping("/applications/my")
    public ResponseEntity<?> getMyApplications(HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            List<ProgramApplication> applications = applicationService.getUserApplications(userId);
            List<ProgramApplicationResponse> responses = applications.stream()
                    .map(ProgramApplicationResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("신청 내역 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 프로그램에 대한 나의 신청 상태 조회
     */
    @GetMapping("/{programId}/my-application")
    public ResponseEntity<?> getMyApplicationForProgram(
            @PathVariable Integer programId,
            HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(Map.of("applied", false));
        }

        try {
            ProgramApplication application = applicationService.getUserApplicationForProgram(userId, programId);

            if (application == null) {
                return ResponseEntity.ok(Map.of("applied", false));
            }

            ProgramApplicationResponse response = ProgramApplicationResponse.from(application);
            return ResponseEntity.ok(Map.of(
                    "applied", true,
                    "application", response
            ));

        } catch (Exception e) {
            log.error("신청 상태 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 프로그램별 신청 내역 조회 (관리자용)
     */
    @GetMapping("/{programId}/applications")
    public ResponseEntity<?> getProgramApplications(
            @PathVariable Integer programId,
            HttpSession session) {

        // 관리자 확인
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "관리자 권한이 필요합니다."));
        }

        try {
            List<ProgramApplication> applications = applicationService.getProgramApplications(programId);
            List<ProgramApplicationResponse> responses = applications.stream()
                    .map(ProgramApplicationResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("프로그램 신청 내역 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }
}
