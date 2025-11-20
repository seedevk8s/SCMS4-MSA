# Phase 2-2: Notification Service 구현

**작성일**: 2025-11-19
**상태**: ✅ 핵심 기능 완료
**예상 시간**: 2-3시간
**실제 소요**: ~2시간

---

## 📋 목표

사용자에게 알림을 전달하는 Notification Service 구현:
- 시스템 알림 CRUD
- 읽음/안읽음 상태 관리
- 알림 템플릿 기능
- 이벤트 기반 알림 (준비)

---

## 🏗 구현 내역

### 1. Entity 계층 (2개 Entity + 3개 Enum)

#### Enum Classes
1. **NotificationType**: SYSTEM, EMAIL, SMS, PUSH, SYSTEM_EMAIL, ALL
2. **NotificationStatus**: UNREAD, READ, DELETED, ARCHIVED
3. **NotificationPriority**: LOW, NORMAL, HIGH, URGENT

#### Entity Classes

**Notification.java** (200+ lines)
- 사용자별 알림 관리
- 읽음/안읽음 상태
- 우선순위별 분류
- 관련 엔티티 연결 (relatedEntityType, relatedEntityId)
- 만료 기능 (expiresAt)
- 이메일 발송 추적 (emailSent, emailSentAt)

```java
@Entity
@Table(name = "notifications")
public class Notification {
    private Long notificationId;
    private Long userId;  // User Service의 userId (FK 없음)
    private String title;
    private String content;
    private NotificationType type;
    private NotificationStatus status;
    private NotificationPriority priority;
    private String relatedEntityType;  // PROGRAM, CONSULTATION 등
    private Long relatedEntityId;
    private String linkUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    // Business Methods
    public void markAsRead() { ... }
    public void markAsDeleted() { ... }
    public boolean isExpired() { ... }
}
```

**NotificationTemplate.java** (180+ lines)
- 재사용 가능한 알림 템플릿
- 변수 치환 기능 ({{userName}}, {{programName}})
- 이메일 제목/본문 템플릿

```java
@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {
    private Long templateId;
    private String templateCode;  // USER_CREATED, PROGRAM_APPROVED 등
    private String templateName;
    private NotificationType type;
    private String titleTemplate;  // "{{userName}}님, 환영합니다!"
    private String contentTemplate;
    private String emailSubjectTemplate;
    private String emailBodyTemplate;
    private String linkUrlTemplate;
    private Boolean active;

    // Business Methods
    public String renderTitle(Map<String, String> variables) { ... }
    public String renderContent(Map<String, String> variables) { ... }
}
```

---

### 2. Repository 계층 (2개 인터페이스)

**NotificationRepository.java**
- 사용자별 알림 조회 (상태별, 유형별)
- 읽지 않은 알림 수 조회
- 긴급 알림 조회
- 만료된 알림 자동 삭제
- 일괄 읽음 처리

```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);
    long countByUserIdAndStatus(Long userId, NotificationStatus status);
    List<Notification> findUrgentNotifications(Long userId);

    @Modifying
    int markAllAsRead(Long userId, LocalDateTime readAt);

    @Modifying
    int deleteExpiredNotifications(LocalDateTime now);
}
```

**NotificationTemplateRepository.java**
- 템플릿 코드로 조회
- 활성화된 템플릿 조회
- 유형별 템플릿 조회

---

### 3. DTO 계층 (2개 클래스)

**NotificationCreateRequest.java**
- 알림 생성 요청
- Validation 포함

**NotificationResponse.java**
- 알림 응답
- Entity → DTO 변환 팩토리 메서드

---

### 4. Service 계층 (1개 클래스)

**NotificationService.java** (150+ lines)
- 알림 생성, 조회, 읽음 처리, 삭제
- 사용자별 알림 관리
- 읽지 않은 알림 수 조회
- 긴급 알림 조회
- 최근 N일 이내 알림 조회

```java
@Service
public class NotificationService {
    public NotificationResponse createNotification(NotificationCreateRequest);
    public List<NotificationResponse> getUserNotifications(Long userId);
    public List<NotificationResponse> getUnreadNotifications(Long userId);
    public void markAsRead(Long notificationId, Long userId);
    public int markAllAsRead(Long userId);
    public void deleteNotification(Long notificationId, Long userId);
    public long getUnreadCount(Long userId);
    public List<NotificationResponse> getUrgentNotifications(Long userId);
}
```

---

### 5. Controller 계층 (1개 클래스)

**NotificationController.java** (11개 엔드포인트)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/notifications` | 내 알림 목록 | Authenticated |
| GET | `/api/notifications/unread` | 읽지 않은 알림 | Authenticated |
| GET | `/api/notifications/urgent` | 긴급 알림 | Authenticated |
| GET | `/api/notifications/{id}` | 알림 상세 | Authenticated |
| POST | `/api/notifications` | 알림 생성 | System |
| POST | `/api/notifications/{id}/read` | 읽음 처리 | Authenticated |
| POST | `/api/notifications/read-all` | 전체 읽음 처리 | Authenticated |
| DELETE | `/api/notifications/{id}` | 알림 삭제 | Authenticated |
| GET | `/api/notifications/unread-count` | 안읽은 알림 수 | Authenticated |
| GET | `/api/notifications/recent?days=7` | 최근 알림 | Authenticated |

---

## 📊 구현 통계

| 계층 | 파일 수 | 총 라인 수 |
|------|---------|------------|
| **Entity** | 2 + 3 Enum | ~450 |
| **Repository** | 2 | ~150 |
| **DTO** | 2 | ~100 |
| **Service** | 1 | ~150 |
| **Controller** | 1 | ~140 |
| **총계** | **11** | **~990** |

**API 엔드포인트**: 11개

---

## 🎯 핵심 기능

### 1. 알림 생성
```java
NotificationCreateRequest request = NotificationCreateRequest.builder()
    .userId(1L)
    .title("프로그램 승인 알림")
    .content("신청하신 'Java 특강'이 승인되었습니다.")
    .type(NotificationType.SYSTEM_EMAIL)
    .priority(NotificationPriority.HIGH)
    .relatedEntityType("PROGRAM")
    .relatedEntityId(123L)
    .linkUrl("/programs/123")
    .build();

notificationService.createNotification(request);
```

### 2. 읽지 않은 알림 조회
```java
List<NotificationResponse> unread = notificationService.getUnreadNotifications(userId);
long count = notificationService.getUnreadCount(userId);
```

### 3. 읽음 처리
```java
// 개별 읽음
notificationService.markAsRead(notificationId, userId);

// 전체 읽음
int count = notificationService.markAllAsRead(userId);
```

---

## 🔄 향후 구현 예정

### 1. RabbitMQ 이벤트 리스너
```java
@Service
public class NotificationEventListener {
    @RabbitListener(queues = "user.created")
    public void handleUserCreated(UserCreatedEvent event) {
        // 회원가입 환영 알림 생성
    }

    @RabbitListener(queues = "program.approved")
    public void handleProgramApproved(ProgramApprovedEvent event) {
        // 프로그램 승인 알림 생성
    }
}
```

### 2. 스케줄링 작업
```java
@Service
public class ScheduledNotificationService {
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    public void deleteExpiredNotifications() {
        notificationRepository.deleteExpiredNotifications(LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 9 * * *")  // 매일 오전 9시
    public void sendProgramDeadlineNotifications() {
        // 프로그램 마감 임박 알림 발송
    }
}
```

### 3. 이메일 발송
```java
@Service
public class EmailNotificationService {
    public void sendEmail(Notification notification) {
        if (notification.getType() == NotificationType.EMAIL ||
            notification.getType() == NotificationType.SYSTEM_EMAIL) {
            // 이메일 발송
            emailService.send(...);
            notification.markEmailSent();
        }
    }
}
```

---

## 🗄️ 데이터베이스 설정

### MySQL 데이터베이스 생성
```sql
CREATE DATABASE scms_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 테이블 자동 생성
JPA `ddl-auto: update` 설정으로 자동 생성됨:
- `notifications` (알림)
- `notification_templates` (알림 템플릿)

---

## 📝 API 테스트 예시

### 1. 알림 생성 (시스템용)
```bash
curl -X POST http://localhost:8082/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "프로그램 승인",
    "content": "Java 특강이 승인되었습니다.",
    "type": "SYSTEM_EMAIL",
    "priority": "HIGH",
    "linkUrl": "/programs/123"
  }'
```

### 2. 읽지 않은 알림 조회
```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8082/api/notifications/unread
```

### 3. 읽지 않은 알림 수
```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8082/api/notifications/unread-count
```

### 4. 알림 읽음 처리
```bash
curl -X POST -H "Authorization: Bearer {token}" \
  http://localhost:8082/api/notifications/1/read
```

---

## ✅ 완료 사항

- [x] Entity 및 Enum 구현 (2개 + 3개)
- [x] Repository 구현 (2개)
- [x] DTO 구현 (핵심 2개)
- [x] Service 구현 (1개)
- [x] Controller 구현 (11개 API)
- [x] 설정 파일 업데이트 (application.yml)
- [x] JPA Auditing 활성화

---

## ⏭️ 다음 단계

### 즉시 수행 가능
- [ ] MySQL 데이터베이스 생성 (`scms_notification`)
- [ ] 서비스 실행 테스트
- [ ] Postman 컬렉션 생성

### Phase 2-3: Program Service
- [ ] 비교과 프로그램 Entity 구현
- [ ] 프로그램 CRUD API
- [ ] 파일 업로드/다운로드
- [ ] 프로그램 리뷰 기능

---

**작성일**: 2025-11-19
**다음 문서**: `07-program-service-implementation.md` (예정)
