package com.scms.app.dto;

import com.scms.app.model.ApplicationStatus;
import com.scms.app.model.ProgramApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로그램 신청 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramApplicationResponse {

    private Integer applicationId;
    private Integer programId;
    private String programTitle;
    private String programDepartment;
    private String programCollege;
    private LocalDateTime programStartDate;
    private LocalDateTime programEndDate;
    private ApplicationStatus status;
    private String statusDescription;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    private String rejectionReason;
    private String notes;
    private boolean cancellable;

    /**
     * Entity를 DTO로 변환
     */
    public static ProgramApplicationResponse from(ProgramApplication application) {
        return ProgramApplicationResponse.builder()
                .applicationId(application.getApplicationId())
                .programId(application.getProgram().getProgramId())
                .programTitle(application.getProgram().getTitle())
                .programDepartment(application.getProgram().getDepartment())
                .programCollege(application.getProgram().getCollege())
                .programStartDate(application.getProgram().getApplicationStartDate())
                .programEndDate(application.getProgram().getApplicationEndDate())
                .status(application.getStatus())
                .statusDescription(application.getStatus().getDescription())
                .appliedAt(application.getAppliedAt())
                .approvedAt(application.getApprovedAt())
                .rejectedAt(application.getRejectedAt())
                .cancelledAt(application.getCancelledAt())
                .completedAt(application.getCompletedAt())
                .rejectionReason(application.getRejectionReason())
                .notes(application.getNotes())
                .cancellable(application.isCancellable())
                .build();
    }
}
