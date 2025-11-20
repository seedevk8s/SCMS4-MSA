package com.scms.portfolio.dto.request;

import com.scms.portfolio.domain.enums.PortfolioStatus;
import com.scms.portfolio.domain.enums.VisibilityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 포트폴리오 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String introduction;

    private PortfolioStatus status;

    private VisibilityLevel visibilityLevel;

    @Size(max = 100)
    private String contactEmail;

    @Size(max = 20)
    private String contactPhone;

    @Size(max = 200)
    private String githubUrl;

    @Size(max = 200)
    private String linkedinUrl;

    @Size(max = 200)
    private String websiteUrl;
}
