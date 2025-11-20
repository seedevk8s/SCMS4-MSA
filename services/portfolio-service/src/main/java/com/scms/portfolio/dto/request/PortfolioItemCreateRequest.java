package com.scms.portfolio.dto.request;

import com.scms.portfolio.domain.enums.PortfolioType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 포트폴리오 항목 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItemCreateRequest {

    @NotNull(message = "항목 타입은 필수입니다.")
    private PortfolioType type;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String subtitle;

    @Size(max = 5000)
    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    private Boolean ongoing = false;

    @Size(max = 100)
    private String role;

    @Size(max = 500)
    private String techStack;

    @Size(max = 500)
    private String url;

    @Size(max = 500)
    private String repositoryUrl;

    @Size(max = 5000)
    private String achievement;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean featured = false;
}
