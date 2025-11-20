package com.scms.notification.controller;

import com.scms.notification.dto.request.NotificationCreateRequest;
import com.scms.notification.dto.response.NotificationResponse;
import com.scms.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림 컨트롤러
 *
 * 엔드포인트:
 * - GET /api/notifications - 내 알림 목록
 * - GET /api/notifications/unread - 읽지 않은 알림
 * - GET /api/notifications/urgent - 긴급 알림
 * - GET /api/notifications/{id} - 알림 상세
 * - POST /api/notifications - 알림 생성 (시스템용)
 * - POST /api/notifications/{id}/read - 읽음 처리
 * - POST /api/notifications/read-all - 전체 읽음 처리
 * - DELETE /api/notifications/{id} - 알림 삭제
 * - GET /api/notifications/unread-count - 안읽은 알림 수
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestAttribute("userId") Long userId
    ) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 조회
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @RequestAttribute("userId") Long userId
    ) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 긴급 알림 조회
     */
    @GetMapping("/urgent")
    public ResponseEntity<List<NotificationResponse>> getUrgentNotifications(
            @RequestAttribute("userId") Long userId
    ) {
        List<NotificationResponse> notifications = notificationService.getUrgentNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 알림 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId
    ) {
        NotificationResponse notification = notificationService.getNotification(id, userId);
        return ResponseEntity.ok(notification);
    }

    /**
     * 알림 생성 (시스템용 - 다른 서비스에서 호출)
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        NotificationResponse notification = notificationService.createNotification(request);
        return ResponseEntity.ok(notification);
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId
    ) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(Map.of("message", "알림을 읽음 처리했습니다."));
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestAttribute("userId") Long userId
    ) {
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "message", "모든 알림을 읽음 처리했습니다.",
                "count", count
        ));
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId
    ) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(Map.of("message", "알림이 삭제되었습니다."));
    }

    /**
     * 읽지 않은 알림 수
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestAttribute("userId") Long userId
    ) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 최근 N일 이내 알림 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NotificationResponse>> getRecentNotifications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "7") int days
    ) {
        List<NotificationResponse> notifications = notificationService.getRecentNotifications(userId, days);
        return ResponseEntity.ok(notifications);
    }
}
