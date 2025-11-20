package com.scms.program.dto.response;

import com.scms.program.domain.entity.Program;
import com.scms.program.domain.enums.ProgramStatus;
import com.scms.program.domain.enums.ProgramType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로그램 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {

    private Long programId;
    private String title;
    private String description;
    private ProgramType type;
    private ProgramStatus status;
    private Long categoryId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime applicationStartDate;
    private LocalDateTime applicationEndDate;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String location;
    private Long createdBy;
    private String instructorName;
    private String instructorContact;
    private Integer mileagePoints;
    private String thumbnailUrl;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProgramResponse from(Program program) {
        return ProgramResponse.builder()
                .programId(program.getProgramId())
                .title(program.getTitle())
                .description(program.getDescription())
                .type(program.getType())
                .status(program.getStatus())
                .categoryId(program.getCategoryId())
                .startDate(program.getStartDate())
                .endDate(program.getEndDate())
                .applicationStartDate(program.getApplicationStartDate())
                .applicationEndDate(program.getApplicationEndDate())
                .maxParticipants(program.getMaxParticipants())
                .currentParticipants(program.getCurrentParticipants())
                .location(program.getLocation())
                .createdBy(program.getCreatedBy())
                .instructorName(program.getInstructorName())
                .instructorContact(program.getInstructorContact())
                .mileagePoints(program.getMileagePoints())
                .thumbnailUrl(program.getThumbnailUrl())
                .viewCount(program.getViewCount())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();
    }
}
