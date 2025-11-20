package com.scms.program.dto.request;

import com.scms.program.domain.enums.ProgramType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로그램 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 5000)
    private String description;

    @NotNull(message = "프로그램 유형은 필수입니다.")
    private ProgramType type;

    private Long categoryId;

    @NotNull(message = "시작 일시는 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료 일시는 필수입니다.")
    private LocalDateTime endDate;

    private LocalDateTime applicationStartDate;

    private LocalDateTime applicationEndDate;

    @NotNull(message = "모집 정원은 필수입니다.")
    @Min(1)
    private Integer maxParticipants;

    @Size(max = 200)
    private String location;

    @Size(max = 100)
    private String instructorName;

    @Size(max = 20)
    private String instructorContact;

    @Min(0)
    private Integer mileagePoints;

    private String thumbnailUrl;
}
