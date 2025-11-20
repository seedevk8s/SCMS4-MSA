package com.scms.portfolio.dto.response;

import com.scms.portfolio.domain.entity.PortfolioItem;
import com.scms.portfolio.domain.enums.PortfolioType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 포트폴리오 항목 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItemResponse {

    private Long itemId;
    private Long portfolioId;
    private PortfolioType type;
    private String title;
    private String subtitle;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean ongoing;
    private String role;
    private String techStack;
    private String url;
    private String repositoryUrl;
    private String achievement;
    private Integer displayOrder;
    private Boolean featured;
    private String thumbnailUrl;
    private List<PortfolioAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity -> Response DTO 변환
     */
    public static PortfolioItemResponse from(PortfolioItem item) {
        return PortfolioItemResponse.builder()
                .itemId(item.getItemId())
                .portfolioId(item.getPortfolio().getPortfolioId())
                .type(item.getType())
                .title(item.getTitle())
                .subtitle(item.getSubtitle())
                .description(item.getDescription())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
                .ongoing(item.getOngoing())
                .role(item.getRole())
                .techStack(item.getTechStack())
                .url(item.getUrl())
                .repositoryUrl(item.getRepositoryUrl())
                .achievement(item.getAchievement())
                .displayOrder(item.getDisplayOrder())
                .featured(item.getFeatured())
                .thumbnailUrl(item.getThumbnailUrl())
                .attachments(item.getAttachments().stream()
                        .filter(attachment -> attachment.getDeletedAt() == null)
                        .map(PortfolioAttachmentResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    /**
     * 간략한 정보만 포함 (첨부파일 제외)
     */
    public static PortfolioItemResponse fromWithoutAttachments(PortfolioItem item) {
        return PortfolioItemResponse.builder()
                .itemId(item.getItemId())
                .portfolioId(item.getPortfolio().getPortfolioId())
                .type(item.getType())
                .title(item.getTitle())
                .subtitle(item.getSubtitle())
                .description(item.getDescription())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
                .ongoing(item.getOngoing())
                .role(item.getRole())
                .techStack(item.getTechStack())
                .url(item.getUrl())
                .repositoryUrl(item.getRepositoryUrl())
                .achievement(item.getAchievement())
                .displayOrder(item.getDisplayOrder())
                .featured(item.getFeatured())
                .thumbnailUrl(item.getThumbnailUrl())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
