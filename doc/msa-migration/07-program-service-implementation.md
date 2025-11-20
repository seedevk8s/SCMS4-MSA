# Program Service 구현 (Phase 2-3)

## 📅 작업 정보
- **작업일**: 2025-11-20
- **Phase**: 2-3
- **서비스명**: Program Service
- **담당**: Claude
- **상태**: ✅ 완료

## 🎯 구현 목표

프로그램 관리 및 신청 기능을 제공하는 마이크로서비스 구현

### 주요 기능
- 프로그램 CRUD (생성, 조회, 수정, 삭제)
- 프로그램 신청 및 관리
- 프로그램 승인/반려 프로세스
- 참가자 정원 관리
- 프로그램 검색 및 필터링
- 마감 임박 프로그램 조회
- 인기 프로그램 조회

## 📊 구현 결과

### 통계
- **파일 개수**: 14개
- **코드 라인 수**: ~1,228 lines
- **API 엔드포인트**: 7개
- **포트**: 8083
- **데이터베이스**: scms_program

### 생성된 파일 목록

#### 1. Domain Layer
```
services/program-service/src/main/java/com/scms/program/domain/
├── entity/
│   ├── Program.java (280+ lines)
│   └── ProgramApplication.java (190+ lines)
└── enums/
    ├── ProgramStatus.java
    ├── ProgramType.java
    └── ApplicationStatus.java
```

#### 2. Repository Layer
```
services/program-service/src/main/java/com/scms/program/repository/
├── ProgramRepository.java
└── ProgramApplicationRepository.java
```

#### 3. Service Layer
```
services/program-service/src/main/java/com/scms/program/service/
└── ProgramService.java
```

#### 4. Controller Layer
```
services/program-service/src/main/java/com/scms/program/controller/
└── ProgramController.java
```

#### 5. DTO Layer
```
services/program-service/src/main/java/com/scms/program/dto/
├── request/
│   ├── ProgramCreateRequest.java
│   └── ProgramApplicationRequest.java
└── response/
    ├── ProgramResponse.java
    └── ProgramApplicationResponse.java
```

## 🏗️ 아키텍처 설계

### 1. Entity 설계

#### Program Entity
```java
@Entity
@Table(name = "programs")
public class Program {
    private Long programId;
    private String title;
    private String description;
    private ProgramType type;
    private ProgramStatus status;
    private Long createdBy;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime applicationStartDate;
    private LocalDateTime applicationEndDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long viewCount;
    // ... 기타 필드
}
```

**주요 기능**:
- 참가자 수 증가/감소 관리
- 정원 초과 검증
- 신청 가능 기간 확인
- 조회수 증가
- Soft Delete 패턴

#### ProgramApplication Entity
```java
@Entity
@Table(name = "program_applications")
public class ProgramApplication {
    private Long applicationId;
    private Long programId;
    private Long userId;
    private ApplicationStatus status;
    private String applicationReason;
    private Long reviewedBy;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private Boolean completed;
    // ... 기타 필드
}
```

**주요 기능**:
- 신청 승인/반려
- 신청 취소
- 참석 확인
- 완료 처리

### 2. Enum 설계

#### ProgramType (5가지)
```java
public enum ProgramType {
    SEMINAR("세미나/특강"),
    WORKSHOP("워크샵/실습"),
    MENTORING("멘토링"),
    NETWORKING("네트워킹"),
    OTHER("기타");
}
```

#### ProgramStatus (5가지)
```java
public enum ProgramStatus {
    DRAFT("임시 저장"),
    APPROVED("승인"),
    REJECTED("반려"),
    ONGOING("진행 중"),
    COMPLETED("종료");
}
```

#### ApplicationStatus (7가지)
```java
public enum ApplicationStatus {
    PENDING("대기 중"),
    APPROVED("승인"),
    REJECTED("반려"),
    CANCELLED("취소"),
    ATTENDED("참석"),
    ABSENT("불참"),
    COMPLETED("완료");
}
```

## 🔌 API 설계

### 1. Program API (7개 엔드포인트)

#### 프로그램 생성
```http
POST /api/programs
Content-Type: application/json
X-User-Id: {userId}

{
  "title": "Spring Boot 세미나",
  "description": "최신 Spring Boot 기술 소개",
  "type": "SEMINAR",
  "maxParticipants": 50,
  "applicationStartDate": "2025-01-01T00:00:00",
  "applicationEndDate": "2025-01-15T23:59:59",
  "startDate": "2025-01-20T14:00:00",
  "endDate": "2025-01-20T17:00:00",
  "location": "대강당"
}
```

#### 프로그램 목록 조회
```http
GET /api/programs
```

#### 승인된 프로그램 조회
```http
GET /api/programs/approved
```

#### 신청 가능한 프로그램 조회
```http
GET /api/programs/available
```

#### 프로그램 상세 조회
```http
GET /api/programs/{programId}
```

#### 프로그램 삭제
```http
DELETE /api/programs/{programId}
X-User-Id: {userId}
```

#### 프로그램 승인
```http
POST /api/programs/{programId}/approve
X-User-Id: {userId}
```

## 💾 데이터베이스 설계

### 테이블 구조

#### programs 테이블
```sql
CREATE TABLE programs (
    program_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    max_participants INT NOT NULL,
    current_participants INT NOT NULL DEFAULT 0,
    application_start_date DATETIME,
    application_end_date DATETIME,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    location VARCHAR(200),
    instructor VARCHAR(100),
    tags VARCHAR(500),
    view_count BIGINT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_start_date (start_date),
    INDEX idx_deleted_at (deleted_at)
);
```

#### program_applications 테이블
```sql
CREATE TABLE program_applications (
    application_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    application_reason TEXT,
    reviewed_by BIGINT,
    review_comment TEXT,
    reviewed_at DATETIME,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME,
    attended BOOLEAN NOT NULL DEFAULT FALSE,
    attended_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_program_user (program_id, user_id),
    INDEX idx_status (status),
    INDEX idx_deleted_at (deleted_at)
);
```

## 🔧 주요 비즈니스 로직

### 1. 참가자 정원 관리

#### 정원 증가 (신청 승인 시)
```java
public void incrementParticipants() {
    if (this.currentParticipants >= this.maxParticipants) {
        throw new IllegalStateException("정원이 초과되었습니다.");
    }
    this.currentParticipants++;
}
```

#### 정원 감소 (신청 취소 시)
```java
public void decrementParticipants() {
    if (this.currentParticipants > 0) {
        this.currentParticipants--;
    }
}
```

### 2. 신청 가능 여부 확인
```java
public boolean isApplicationAvailable() {
    LocalDateTime now = LocalDateTime.now();
    return this.status == ProgramStatus.APPROVED
            && !isFull()
            && (applicationStartDate == null || now.isAfter(applicationStartDate))
            && (applicationEndDate == null || now.isBefore(applicationEndDate));
}
```

### 3. 신청 승인 프로세스
```java
@Transactional
public void approveApplication(Long applicationId, Long reviewerId, String comment) {
    ProgramApplication application = getApplicationEntity(applicationId);
    Program program = getProgramEntity(application.getProgramId());

    // 정원 확인
    if (program.isFull()) {
        throw new ApiException(ErrorCode.PROGRAM_FULL);
    }

    // 승인 처리
    application.approve(reviewerId, comment);
    program.incrementParticipants();
}
```

## 📝 Repository 쿼리 메서드

### 복잡한 쿼리 예시

#### 신청 가능한 프로그램 조회
```java
@Query("SELECT p FROM Program p WHERE p.status = 'APPROVED' " +
        "AND p.currentParticipants < p.maxParticipants " +
        "AND (p.applicationStartDate IS NULL OR p.applicationStartDate <= :now) " +
        "AND (p.applicationEndDate IS NULL OR p.applicationEndDate >= :now) " +
        "AND p.deletedAt IS NULL " +
        "ORDER BY p.startDate ASC")
List<Program> findAvailablePrograms(@Param("now") LocalDateTime now);
```

#### 마감 임박 프로그램 조회
```java
@Query("SELECT p FROM Program p WHERE p.status = 'APPROVED' " +
        "AND p.applicationEndDate BETWEEN :now AND :deadline " +
        "AND p.deletedAt IS NULL " +
        "ORDER BY p.applicationEndDate ASC")
List<Program> findDeadlineSoonPrograms(
    @Param("now") LocalDateTime now,
    @Param("deadline") LocalDateTime deadline
);
```

#### 인기 프로그램 조회
```java
@Query("SELECT p FROM Program p WHERE p.status = 'APPROVED' " +
        "AND p.deletedAt IS NULL " +
        "ORDER BY p.viewCount DESC, p.currentParticipants DESC")
List<Program> findPopularPrograms();
```

## 🔐 보안 및 권한 관리

### 권한 검증
```java
private void validateOwnership(Program program, Long userId) {
    if (!program.getCreatedBy().equals(userId)) {
        throw new ApiException(ErrorCode.FORBIDDEN,
            "본인이 생성한 프로그램만 수정/삭제할 수 있습니다.");
    }
}
```

## ⚙️ 설정 파일

### application.yml
```yaml
spring:
  application:
    name: program-service
  datasource:
    url: jdbc:mysql://localhost:3306/scms_program
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8083

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    runtimeOnly 'com.mysql:mysql-connector-j'
    implementation project(':common-library:common-dto')
    implementation project(':common-library:common-exception')
    implementation project(':common-library:common-util')
}
```

## 🎓 기술적 특징

### 1. Soft Delete 패턴
- 모든 엔티티에 `deletedAt` 필드 추가
- 물리적 삭제 대신 논리적 삭제
- 데이터 복구 가능

### 2. JPA Auditing
- `@CreatedDate`: 생성일시 자동 관리
- `@LastModifiedDate`: 수정일시 자동 관리
- `@EntityListeners(AuditingEntityListener.class)` 적용

### 3. 낙관적 동시성 제어 준비
- 참가자 수 증가 시 동시성 이슈 대비
- `@Version` 필드 추가 가능 (추후 필요 시)

### 4. Database Per Service 패턴
- 독립적인 데이터베이스 (scms_program)
- 다른 서비스와 데이터베이스 분리
- FK 제약조건 없이 ID만 저장

## 📈 성능 최적화

### 1. 인덱스 전략
```sql
-- 복합 인덱스
INDEX idx_status_start_date (status, start_date)

-- 조회 성능 향상
INDEX idx_view_count (view_count DESC)
INDEX idx_participants (current_participants DESC)
```

### 2. 쿼리 최적화
- N+1 문제 방지를 위한 Fetch Join 준비
- 페이징 처리 준비
- 필요한 필드만 조회하는 DTO Projection

## 🧪 테스트 시나리오

### 1. 프로그램 생성
```bash
curl -X POST http://localhost:8083/api/programs \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "Spring Boot 세미나",
    "description": "최신 Spring Boot 기술",
    "type": "SEMINAR",
    "maxParticipants": 50,
    "startDate": "2025-01-20T14:00:00",
    "endDate": "2025-01-20T17:00:00"
  }'
```

### 2. 신청 가능한 프로그램 조회
```bash
curl http://localhost:8083/api/programs/available
```

### 3. 프로그램 승인
```bash
curl -X POST http://localhost:8083/api/programs/1/approve \
  -H "X-User-Id: 1"
```

## ✅ 구현 완료 체크리스트

- [x] Program Entity 구현
- [x] ProgramApplication Entity 구현
- [x] Enum 클래스 구현 (3개)
- [x] Repository 구현 (2개)
- [x] Service 구현
- [x] Controller 구현
- [x] DTO 구현 (4개)
- [x] 비즈니스 로직 구현
- [x] 정원 관리 로직
- [x] 신청 승인 프로세스
- [x] 데이터베이스 초기화 스크립트
- [x] Application 클래스 작성
- [x] 설정 파일 작성
- [x] Eureka 등록

## 🔄 다음 단계

### 단기 계획
- [ ] 프로그램 이미지 업로드 기능
- [ ] 프로그램 평가 기능
- [ ] 프로그램 출석 체크 기능

### 장기 계획
- [ ] 프로그램 알림 자동 발송 (Notification Service 연동)
- [ ] 프로그램 통계 대시보드
- [ ] 프로그램 추천 알고리즘

## 🐛 알려진 이슈

없음

## 📚 참고 자료

- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Cloud Netflix Eureka](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
- [Database Per Service Pattern](https://microservices.io/patterns/data/database-per-service.html)

## 📅 작업 이력

- 2025-11-20: Program Service 구현 완료
- 2025-11-20: Git 커밋 및 푸시 완료 (commit: bc5252e)
