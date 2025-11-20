package com.scms.notification.repository;

import com.scms.notification.domain.entity.NotificationTemplate;
import com.scms.notification.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 템플릿 Repository
 *
 * 주요 쿼리:
 * - 템플릿 코드로 조회
 * - 활성화된 템플릿 조회
 * - 유형별 템플릿 조회
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * 템플릿 코드로 조회
     */
    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    /**
     * 템플릿 코드로 조회 (활성화된 것만)
     */
    Optional<NotificationTemplate> findByTemplateCodeAndActiveTrue(String templateCode);

    /**
     * 템플릿 코드 존재 여부
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * 활성화된 모든 템플릿 조회
     */
    List<NotificationTemplate> findByActiveTrueOrderByTemplateCodeAsc();

    /**
     * 유형별 템플릿 조회
     */
    List<NotificationTemplate> findByTypeAndActiveTrueOrderByTemplateCodeAsc(NotificationType type);

    /**
     * 템플릿 이름으로 검색
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.templateName LIKE %:keyword% " +
            "ORDER BY t.templateCode ASC")
    List<NotificationTemplate> searchByTemplateName(@Param("keyword") String keyword);

    /**
     * 생성자별 템플릿 조회
     */
    List<NotificationTemplate> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * 전체 템플릿 수
     */
    long count();

    /**
     * 활성화된 템플릿 수
     */
    long countByActiveTrue();
}
