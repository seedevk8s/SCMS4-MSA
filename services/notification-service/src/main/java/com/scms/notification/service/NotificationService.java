package com.scms.notification.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.notification.domain.entity.Notification;
import com.scms.notification.domain.enums.NotificationStatus;
import com.scms.notification.dto.request.NotificationCreateRequest;
import com.scms.notification.dto.response.NotificationResponse;
import com.scms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 서비스
 *
 * 주요 기능:
 * - 알림 생성, 조회, 읽음 처리, 삭제
 * - 사용자별 알림 관리
 * - 읽지 않은 알림 수 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성
     */
    @Transactional
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .priority(request.getPriority())
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .linkUrl(request.getLinkUrl())
                .expiresAt(request.getExpiresAt())
                .templateId(request.getTemplateId())
                .metadata(request.getMetadata())
                .status(NotificationStatus.UNREAD)
                .emailSent(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("알림 생성 완료: notificationId={}, userId={}, type={}",
                saved.getNotificationId(), saved.getUserId(), saved.getType());

        return NotificationResponse.from(saved);
    }

    /**
     * 사용자의 모든 알림 조회
     */
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 읽지 않은 알림 조회
     */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, NotificationStatus.UNREAD)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 알림 상세 조회
     */
    public NotificationResponse getNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 권한 확인 (본인의 알림만 조회 가능)
        if (!notification.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인의 알림만 조회할 수 있습니다.");
        }

        return NotificationResponse.from(notification);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 권한 확인
        if (!notification.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        notification.markAsRead();
        notificationRepository.save(notification);
        log.info("알림 읽음 처리: notificationId={}", notificationId);
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("모든 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 권한 확인
        if (!notification.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        notification.markAsDeleted();
        notificationRepository.save(notification);
        log.info("알림 삭제: notificationId={}", notificationId);
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    /**
     * 최근 N일 이내 알림 조회
     */
    public List<NotificationResponse> getRecentNotifications(Long userId, int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return notificationRepository.findRecentNotifications(userId, sinceDate)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 긴급 알림 조회
     */
    public List<NotificationResponse> getUrgentNotifications(Long userId) {
        return notificationRepository.findUrgentNotifications(userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }
}
