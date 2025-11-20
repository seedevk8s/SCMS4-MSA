package com.scms.portfolio.dto.response;

import com.scms.portfolio.domain.entity.Portfolio;
import com.scms.portfolio.domain.enums.PortfolioStatus;
import com.scms.portfolio.domain.enums.VisibilityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 포트폴리오 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {

    private Long portfolioId;
    private Long userId;
    private String title;
    private String introduction;
    private PortfolioStatus status;
    private VisibilityLevel visibilityLevel;
    private String profileImageUrl;
    private String coverImageUrl;
    private String contactEmail;
    private String contactPhone;
    private String githubUrl;
    private String linkedinUrl;
    private String websiteUrl;
    private Long viewCount;
    private Long likeCount;
    private Long shareCount;
    private LocalDateTime publishedAt;
    private List<PortfolioItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity -> Response DTO 변환
     */
    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getPortfolioId())
                .userId(portfolio.getUserId())
                .title(portfolio.getTitle())
                .introduction(portfolio.getIntroduction())
                .status(portfolio.getStatus())
                .visibilityLevel(portfolio.getVisibilityLevel())
                .profileImageUrl(portfolio.getProfileImageUrl())
                .coverImageUrl(portfolio.getCoverImageUrl())
                .contactEmail(portfolio.getContactEmail())
                .contactPhone(portfolio.getContactPhone())
                .githubUrl(portfolio.getGithubUrl())
                .linkedinUrl(portfolio.getLinkedinUrl())
                .websiteUrl(portfolio.getWebsiteUrl())
                .viewCount(portfolio.getViewCount())
                .likeCount(portfolio.getLikeCount())
                .shareCount(portfolio.getShareCount())
                .publishedAt(portfolio.getPublishedAt())
                .items(portfolio.getItems().stream()
                        .filter(item -> item.getDeletedAt() == null)
                        .map(PortfolioItemResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    /**
     * 간략한 정보만 포함 (항목 제외)
     */
    public static PortfolioResponse fromWithoutItems(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getPortfolioId())
                .userId(portfolio.getUserId())
                .title(portfolio.getTitle())
                .introduction(portfolio.getIntroduction())
                .status(portfolio.getStatus())
                .visibilityLevel(portfolio.getVisibilityLevel())
                .profileImageUrl(portfolio.getProfileImageUrl())
                .coverImageUrl(portfolio.getCoverImageUrl())
                .viewCount(portfolio.getViewCount())
                .likeCount(portfolio.getLikeCount())
                .shareCount(portfolio.getShareCount())
                .publishedAt(portfolio.getPublishedAt())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}
