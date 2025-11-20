package com.scms.program.controller;

import com.scms.program.dto.request.ProgramCreateRequest;
import com.scms.program.dto.response.ProgramResponse;
import com.scms.program.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 프로그램 컨트롤러
 *
 * 엔드포인트:
 * - GET /api/programs - 전체 프로그램 목록
 * - GET /api/programs/approved - 승인된 프로그램
 * - GET /api/programs/available - 신청 가능한 프로그램
 * - GET /api/programs/{id} - 프로그램 상세
 * - POST /api/programs - 프로그램 생성
 * - DELETE /api/programs/{id} - 프로그램 삭제
 * - POST /api/programs/{id}/approve - 프로그램 승인 (ADMIN)
 */
@Slf4j
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @GetMapping
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        List<ProgramResponse> programs = programService.getAllPrograms();
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<ProgramResponse>> getApprovedPrograms() {
        List<ProgramResponse> programs = programService.getApprovedPrograms();
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProgramResponse>> getAvailablePrograms() {
        List<ProgramResponse> programs = programService.getAvailablePrograms();
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponse> getProgram(@PathVariable Long id) {
        ProgramResponse program = programService.getProgram(id);
        return ResponseEntity.ok(program);
    }

    @PostMapping
    public ResponseEntity<ProgramResponse> createProgram(
            @Valid @RequestBody ProgramCreateRequest request,
            @RequestAttribute("userId") Long userId
    ) {
        ProgramResponse program = programService.createProgram(request, userId);
        return ResponseEntity.ok(program);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok(Map.of("message", "프로그램이 삭제되었습니다."));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveProgram(@PathVariable Long id) {
        programService.approveProgram(id);
        return ResponseEntity.ok(Map.of("message", "프로그램이 승인되었습니다."));
    }
}
