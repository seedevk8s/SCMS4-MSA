package com.scms.program.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프로그램 신청 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramApplicationRequest {

    @Size(max = 1000)
    private String motivation;  // 신청 동기
}
