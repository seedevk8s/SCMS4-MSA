package com.scms.notification.repository;

import com.scms.notification.domain.entity.Notification;
import com.scms.notification.domain.enums.NotificationStatus;
import com.scms.notification.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알림 Repository
 *
 * 주요 쿼리:
 * - 사용자별 알림 조회 (상태별, 유형별)
 * - 읽지 않은 알림 수 조회
 * - 만료된 알림 자동 삭제
 * - 일괄 읽음 처리
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 모든 알림 조회 (삭제되지 않은)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status != 'DELETED' ORDER BY n.createdAt DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 알림 조회 (상태별)
     */
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);

    /**
     * 사용자의 알림 조회 (유형별)
     */
    List<Notification> findByUserIdAndTypeAndStatusNotOrderByCreatedAtDesc(
            Long userId,
            NotificationType type,
            NotificationStatus status
    );

    /**
     * 사용자의 읽지 않은 알림 조회
     */
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);

    /**
     * 사용자의 읽지 않은 알림 수
     */
    long countByUserIdAndStatus(Long userId, NotificationStatus status);

    /**
     * 사용자의 긴급 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.priority = 'URGENT' AND n.status = 'UNREAD' " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findUrgentNotifications(@Param("userId") Long userId);

    /**
     * 특정 관련 엔티티에 대한 알림 조회
     */
    List<Notification> findByUserIdAndRelatedEntityTypeAndRelatedEntityId(
            Long userId,
            String relatedEntityType,
            Long relatedEntityId
    );

    /**
     * 최근 N일 이내 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.createdAt >= :sinceDate AND n.status != 'DELETED' " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDateTime sinceDate
    );

    /**
     * 만료된 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL " +
            "AND n.expiresAt < :now AND n.status != 'DELETED'")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * 이메일 미발송 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.emailSent = false " +
            "AND (n.type = 'EMAIL' OR n.type = 'SYSTEM_EMAIL' OR n.type = 'ALL') " +
            "AND n.createdAt >= :sinceDate")
    List<Notification> findPendingEmailNotifications(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 사용자의 특정 기간 동안 읽은 알림 (자동 삭제 대상)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.status = 'READ' AND n.readAt < :beforeDate")
    List<Notification> findOldReadNotifications(
            @Param("userId") Long userId,
            @Param("beforeDate") LocalDateTime beforeDate
    );

    /**
     * 사용자의 모든 읽지 않은 알림을 읽음으로 일괄 변경
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt " +
            "WHERE n.userId = :userId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * 만료된 알림 일괄 삭제
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'DELETED' " +
            "WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now AND n.status != 'DELETED'")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * 오래된 읽은 알림 일괄 삭제 (30일 이상 경과)
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'DELETED' " +
            "WHERE n.status = 'READ' AND n.readAt < :beforeDate")
    int deleteOldReadNotifications(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 사용자의 알림 통계
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n " +
            "WHERE n.userId = :userId GROUP BY n.status")
    List<Object[]> getUserNotificationStats(@Param("userId") Long userId);

    /**
     * 템플릿 ID로 알림 조회
     */
    List<Notification> findByTemplateId(Long templateId);
}
