package com.scms.program.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.program.domain.entity.Program;
import com.scms.program.domain.enums.ProgramStatus;
import com.scms.program.dto.request.ProgramCreateRequest;
import com.scms.program.dto.response.ProgramResponse;
import com.scms.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;

    @Transactional
    public ProgramResponse createProgram(ProgramCreateRequest request, Long userId) {
        Program program = Program.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .categoryId(request.getCategoryId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .applicationStartDate(request.getApplicationStartDate())
                .applicationEndDate(request.getApplicationEndDate())
                .maxParticipants(request.getMaxParticipants())
                .location(request.getLocation())
                .createdBy(userId)
                .instructorName(request.getInstructorName())
                .instructorContact(request.getInstructorContact())
                .mileagePoints(request.getMileagePoints())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(ProgramStatus.DRAFT)
                .currentParticipants(0)
                .viewCount(0)
                .build();

        Program saved = programRepository.save(program);
        log.info("프로그램 생성: programId={}, title={}", saved.getProgramId(), saved.getTitle());
        return ProgramResponse.from(saved);
    }

    public ProgramResponse getProgram(Long programId) {
        Program program = programRepository.findByProgramIdAndDeletedAtIsNull(programId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROGRAM_NOT_FOUND));
        return ProgramResponse.from(program);
    }

    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAll().stream()
                .filter(p -> p.getDeletedAt() == null)
                .map(ProgramResponse::from)
                .collect(Collectors.toList());
    }

    public List<ProgramResponse> getApprovedPrograms() {
        return programRepository.findApprovedPrograms().stream()
                .map(ProgramResponse::from)
                .collect(Collectors.toList());
    }

    public List<ProgramResponse> getAvailablePrograms() {
        return programRepository.findAvailablePrograms(LocalDateTime.now()).stream()
                .map(ProgramResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProgram(Long programId) {
        Program program = programRepository.findByProgramIdAndDeletedAtIsNull(programId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROGRAM_NOT_FOUND));
        program.delete();
        programRepository.save(program);
        log.info("프로그램 삭제: programId={}", programId);
    }

    @Transactional
    public void approveProgram(Long programId) {
        Program program = programRepository.findByProgramIdAndDeletedAtIsNull(programId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROGRAM_NOT_FOUND));
        program.approve();
        programRepository.save(program);
        log.info("프로그램 승인: programId={}", programId);
    }
}
