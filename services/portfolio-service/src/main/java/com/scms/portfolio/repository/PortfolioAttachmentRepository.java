package com.scms.portfolio.repository;

import com.scms.portfolio.domain.entity.PortfolioAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 첨부 파일 Repository
 */
@Repository
public interface PortfolioAttachmentRepository extends JpaRepository<PortfolioAttachment, Long> {

    /**
     * 포트폴리오 항목별 첨부 파일 목록
     */
    List<PortfolioAttachment> findByPortfolioItem_ItemIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long itemId);

    /**
     * 첨부 파일 상세 조회
     */
    Optional<PortfolioAttachment> findByAttachmentIdAndDeletedAtIsNull(Long attachmentId);

    /**
     * 포트폴리오 항목별 첨부 파일 개수
     */
    long countByPortfolioItem_ItemIdAndDeletedAtIsNull(Long itemId);

    /**
     * 이미지 파일만 조회
     */
    List<PortfolioAttachment> findByPortfolioItem_ItemIdAndFileTypeStartingWithAndDeletedAtIsNullOrderByDisplayOrderAsc(
            Long itemId,
            String fileTypePrefix
    );
}
