package com.scms.portfolio.dto.response;

import com.scms.portfolio.domain.entity.PortfolioAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포트폴리오 첨부 파일 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAttachmentResponse {

    private Long attachmentId;
    private Long itemId;
    private String originalFilename;
    private String storedFilename;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer displayOrder;
    private Boolean isImage;
    private Boolean isPdf;
    private LocalDateTime createdAt;

    /**
     * Entity -> Response DTO 변환
     */
    public static PortfolioAttachmentResponse from(PortfolioAttachment attachment) {
        return PortfolioAttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .itemId(attachment.getPortfolioItem().getItemId())
                .originalFilename(attachment.getOriginalFilename())
                .storedFilename(attachment.getStoredFilename())
                .fileUrl(attachment.getFileUrl())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .displayOrder(attachment.getDisplayOrder())
                .isImage(attachment.isImage())
                .isPdf(attachment.isPdf())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
