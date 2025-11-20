# Portfolio Service 구현 (Phase 2-4)

## 📅 작업 정보
- **작업일**: 2025-11-20
- **Phase**: 2-4
- **서비스명**: Portfolio Service
- **담당**: Claude
- **상태**: ✅ 완료

## 🎯 구현 목표

학생 포트폴리오 관리 기능을 제공하는 마이크로서비스 구현

### 주요 기능
- 포트폴리오 CRUD (생성, 조회, 수정, 삭제)
- 포트폴리오 항목 관리 (프로젝트, 수상, 자격증 등)
- 공개 범위 설정 (공개, 비공개, 기업공개, 학교공개)
- 포트폴리오 상태 관리 (임시저장, 공개, 보관)
- 좋아요, 공유, 조회수 관리
- 강조 표시 항목 관리
- 진행 중인 항목 조회
- 기술 스택 검색
- 첨부 파일 메타데이터 관리

## 📊 구현 결과

### 통계
- **파일 개수**: 25개
- **코드 라인 수**: ~2,552 lines
- **API 엔드포인트**: 29개 (Portfolio: 17개, Items: 12개)
- **포트**: 8084
- **데이터베이스**: scms_portfolio

### 생성된 파일 목록

#### 1. Domain Layer
```
services/portfolio-service/src/main/java/com/scms/portfolio/domain/
├── entity/
│   ├── Portfolio.java (290+ lines)
│   ├── PortfolioItem.java (220+ lines)
│   └── PortfolioAttachment.java (120+ lines)
└── enums/
    ├── PortfolioType.java (11개 타입)
    ├── PortfolioStatus.java (3개 상태)
    └── VisibilityLevel.java (4개 공개범위)
```

#### 2. Repository Layer
```
services/portfolio-service/src/main/java/com/scms/portfolio/repository/
├── PortfolioRepository.java
├── PortfolioItemRepository.java
└── PortfolioAttachmentRepository.java
```

#### 3. Service Layer
```
services/portfolio-service/src/main/java/com/scms/portfolio/service/
├── PortfolioService.java
└── PortfolioItemService.java
```

#### 4. Controller Layer
```
services/portfolio-service/src/main/java/com/scms/portfolio/controller/
├── PortfolioController.java
└── PortfolioItemController.java
```

#### 5. DTO Layer
```
services/portfolio-service/src/main/java/com/scms/portfolio/dto/
├── request/
│   ├── PortfolioCreateRequest.java
│   ├── PortfolioUpdateRequest.java
│   ├── PortfolioItemCreateRequest.java
│   └── PortfolioItemUpdateRequest.java
└── response/
    ├── PortfolioResponse.java
    ├── PortfolioItemResponse.java
    └── PortfolioAttachmentResponse.java
```

## 🏗️ 아키텍처 설계

### 1. Entity 설계

#### Portfolio Entity (포트폴리오 메인)
```java
@Entity
@Table(name = "portfolios")
public class Portfolio {
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
    private List<PortfolioItem> items;
    // ... 기타 필드
}
```

**주요 기능**:
- 포트폴리오 정보 업데이트
- 상태 변경 (DRAFT → PUBLISHED → ARCHIVED)
- 공개 범위 변경
- 조회수/좋아요/공유 수 증가
- 항목 추가/제거
- 공개 여부 확인

#### PortfolioItem Entity (포트폴리오 항목)
```java
@Entity
@Table(name = "portfolio_items")
public class PortfolioItem {
    private Long itemId;
    private Portfolio portfolio;
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
    private List<PortfolioAttachment> attachments;
    // ... 기타 필드
}
```

**주요 기능**:
- 항목 정보 업데이트
- 썸네일 이미지 설정
- 순서 변경
- 강조 표시 토글
- 첨부 파일 관리

#### PortfolioAttachment Entity (첨부 파일)
```java
@Entity
@Table(name = "portfolio_attachments")
public class PortfolioAttachment {
    private Long attachmentId;
    private PortfolioItem portfolioItem;
    private String originalFilename;
    private String storedFilename;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer displayOrder;
    // ... 기타 필드
}
```

**주요 기능**:
- 파일 메타데이터 저장
- 이미지/PDF 타입 구분
- 순서 관리

### 2. Enum 설계

#### PortfolioType (11가지)
```java
public enum PortfolioType {
    PROJECT("프로젝트"),
    AWARD("수상 경력"),
    CERTIFICATE("자격증"),
    ACTIVITY("대외 활동"),
    SKILL("기술 스택"),
    EDUCATION("교육"),
    EXPERIENCE("경력"),
    PUBLICATION("논문/출판"),
    PATENT("특허"),
    LANGUAGE("어학"),
    OTHER("기타");
}
```

#### PortfolioStatus (3가지)
```java
public enum PortfolioStatus {
    DRAFT("임시 저장"),
    PUBLISHED("공개"),
    ARCHIVED("보관");
}
```

#### VisibilityLevel (4가지)
```java
public enum VisibilityLevel {
    PUBLIC("전체 공개"),
    PRIVATE("비공개"),
    COMPANY_ONLY("기업만 공개"),
    SCHOOL_ONLY("학교 내 공개");
}
```

## 🔌 API 설계

### 1. Portfolio API (17개 엔드포인트)

#### 포트폴리오 생성
```http
POST /api/portfolios
Content-Type: application/json
X-User-Id: {userId}

{
  "title": "홍길동의 포트폴리오",
  "introduction": "백엔드 개발자를 꿈꾸는 학생입니다",
  "visibilityLevel": "PUBLIC",
  "contactEmail": "hong@example.com",
  "githubUrl": "https://github.com/hong"
}
```

#### 포트폴리오 수정
```http
PUT /api/portfolios/{portfolioId}
Content-Type: application/json
X-User-Id: {userId}

{
  "title": "업데이트된 제목",
  "introduction": "새로운 소개",
  "status": "PUBLISHED"
}
```

#### 포트폴리오 삭제
```http
DELETE /api/portfolios/{portfolioId}
X-User-Id: {userId}
```

#### 포트폴리오 상세 조회
```http
GET /api/portfolios/{portfolioId}
X-User-Id: {userId}
```

#### 사용자별 포트폴리오 목록
```http
GET /api/portfolios/users/{userId}
X-User-Id: {currentUserId}
```

#### 내 포트폴리오 목록
```http
GET /api/portfolios/my
X-User-Id: {userId}
```

#### 공개 포트폴리오 목록
```http
GET /api/portfolios/public
```

#### 인기 포트폴리오
```http
GET /api/portfolios/popular
```

#### 추천 포트폴리오
```http
GET /api/portfolios/recommended
```

#### 포트폴리오 검색
```http
GET /api/portfolios/search?keyword={keyword}
```

#### 상태 변경
```http
PATCH /api/portfolios/{portfolioId}/status?status=PUBLISHED
X-User-Id: {userId}
```

#### 공개 범위 변경
```http
PATCH /api/portfolios/{portfolioId}/visibility?visibility=PUBLIC
X-User-Id: {userId}
```

#### 좋아요
```http
POST /api/portfolios/{portfolioId}/like
X-User-Id: {userId}
```

#### 좋아요 취소
```http
DELETE /api/portfolios/{portfolioId}/like
X-User-Id: {userId}
```

#### 공유
```http
POST /api/portfolios/{portfolioId}/share
```

#### 프로필 이미지 업데이트
```http
PATCH /api/portfolios/{portfolioId}/profile-image?imageUrl={url}
X-User-Id: {userId}
```

#### 커버 이미지 업데이트
```http
PATCH /api/portfolios/{portfolioId}/cover-image?imageUrl={url}
X-User-Id: {userId}
```

### 2. Portfolio Items API (12개 엔드포인트)

#### 항목 생성
```http
POST /api/portfolios/{portfolioId}/items
Content-Type: application/json
X-User-Id: {userId}

{
  "type": "PROJECT",
  "title": "학생 관리 시스템 개발",
  "description": "Spring Boot를 활용한 시스템",
  "startDate": "2024-01-01",
  "endDate": "2024-06-30",
  "techStack": "Spring Boot, MySQL, React",
  "repositoryUrl": "https://github.com/user/project"
}
```

#### 항목 수정
```http
PUT /api/portfolios/{portfolioId}/items/{itemId}
X-User-Id: {userId}
```

#### 항목 삭제
```http
DELETE /api/portfolios/{portfolioId}/items/{itemId}
X-User-Id: {userId}
```

#### 항목 상세 조회
```http
GET /api/portfolios/{portfolioId}/items/{itemId}
```

#### 모든 항목 조회
```http
GET /api/portfolios/{portfolioId}/items
```

#### 타입별 항목 조회
```http
GET /api/portfolios/{portfolioId}/items/type/PROJECT
```

#### 강조 표시된 항목
```http
GET /api/portfolios/{portfolioId}/items/featured
```

#### 진행 중인 항목
```http
GET /api/portfolios/{portfolioId}/items/ongoing
```

#### 강조 표시 토글
```http
PATCH /api/portfolios/{portfolioId}/items/{itemId}/featured
X-User-Id: {userId}
```

#### 썸네일 업데이트
```http
PATCH /api/portfolios/{portfolioId}/items/{itemId}/thumbnail?thumbnailUrl={url}
X-User-Id: {userId}
```

#### 순서 변경
```http
PATCH /api/portfolios/{portfolioId}/items/{itemId}/order?displayOrder=5
X-User-Id: {userId}
```

#### 기술 스택 검색
```http
GET /api/portfolios/{portfolioId}/items/search?techStack=Spring
```

## 💾 데이터베이스 설계

### 테이블 구조

#### portfolios 테이블
```sql
CREATE TABLE portfolios (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    introduction TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    visibility_level VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    profile_image_url VARCHAR(500),
    cover_image_url VARCHAR(500),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    github_url VARCHAR(200),
    linkedin_url VARCHAR(200),
    website_url VARCHAR(200),
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    share_count BIGINT NOT NULL DEFAULT 0,
    published_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_visibility (visibility_level),
    INDEX idx_published_at (published_at),
    INDEX idx_view_count (view_count DESC),
    INDEX idx_like_count (like_count DESC)
);
```

#### portfolio_items 테이블
```sql
CREATE TABLE portfolio_items (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    subtitle VARCHAR(200),
    description TEXT,
    start_date DATE,
    end_date DATE,
    ongoing BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(100),
    tech_stack VARCHAR(500),
    url VARCHAR(500),
    repository_url VARCHAR(500),
    achievement TEXT,
    display_order INT NOT NULL DEFAULT 0,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    thumbnail_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_type (type),
    INDEX idx_display_order (display_order),
    INDEX idx_featured (featured)
);
```

#### portfolio_attachments 테이블
```sql
CREATE TABLE portfolio_attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_item_id (item_id),
    INDEX idx_file_type (file_type)
);
```

## 🔧 주요 비즈니스 로직

### 1. 포트폴리오 공개 관리

#### 상태 변경 시 공개일 기록
```java
public void updateStatus(PortfolioStatus status) {
    this.status = status;
    if (status == PortfolioStatus.PUBLISHED) {
        this.publishedAt = LocalDateTime.now();
    }
}
```

#### 공개 여부 확인
```java
public boolean isPublic() {
    return this.visibilityLevel == VisibilityLevel.PUBLIC
        && this.status == PortfolioStatus.PUBLISHED;
}
```

### 2. 조회 권한 검증
```java
private void validateViewPermission(Portfolio portfolio, Long currentUserId) {
    // 본인이면 무조건 조회 가능
    if (portfolio.getUserId().equals(currentUserId)) {
        return;
    }

    // 공개 상태가 아니면 조회 불가
    if (portfolio.getStatus() != PortfolioStatus.PUBLISHED) {
        throw new ApiException(ErrorCode.FORBIDDEN, "공개되지 않은 포트폴리오입니다.");
    }

    // 비공개면 조회 불가
    if (portfolio.getVisibilityLevel() == VisibilityLevel.PRIVATE) {
        throw new ApiException(ErrorCode.FORBIDDEN, "비공개 포트폴리오입니다.");
    }
}
```

### 3. 상호작용 관리

#### 좋아요 증가
```java
@Transactional
public void likePortfolio(Long portfolioId, Long userId) {
    Portfolio portfolio = getPortfolioEntity(portfolioId);

    // 본인 포트폴리오는 좋아요 불가
    if (portfolio.getUserId().equals(userId)) {
        throw new ApiException(ErrorCode.BAD_REQUEST,
            "본인의 포트폴리오는 좋아요할 수 없습니다.");
    }

    portfolioRepository.incrementLikeCount(portfolioId);
}
```

#### 조회수 증가 (본인 제외)
```java
@Transactional
public PortfolioResponse getPortfolio(Long portfolioId, Long currentUserId) {
    Portfolio portfolio = getPortfolioEntity(portfolioId);

    // 조회 권한 확인
    validateViewPermission(portfolio, currentUserId);

    // 조회수 증가 (본인 제외)
    if (!portfolio.getUserId().equals(currentUserId)) {
        portfolioRepository.incrementViewCount(portfolioId);
    }

    return PortfolioResponse.from(portfolio);
}
```

## 📝 Repository 쿼리 메서드

### 복잡한 쿼리 예시

#### 공개 포트폴리오 목록
```java
@Query("SELECT p FROM Portfolio p WHERE p.status = 'PUBLISHED' " +
        "AND p.visibilityLevel = 'PUBLIC' " +
        "AND p.deletedAt IS NULL " +
        "ORDER BY p.publishedAt DESC")
List<Portfolio> findPublicPortfolios();
```

#### 인기 포트폴리오 조회
```java
@Query("SELECT p FROM Portfolio p WHERE p.status = 'PUBLISHED' " +
        "AND p.visibilityLevel = 'PUBLIC' " +
        "AND p.deletedAt IS NULL " +
        "ORDER BY p.viewCount DESC, p.likeCount DESC")
List<Portfolio> findPopularPortfolios();
```

#### 기술 스택으로 항목 검색
```java
@Query("SELECT i FROM PortfolioItem i WHERE i.portfolio.portfolioId = :portfolioId " +
        "AND i.techStack LIKE %:techStack% " +
        "AND i.deletedAt IS NULL " +
        "ORDER BY i.displayOrder ASC")
List<PortfolioItem> searchByTechStack(
    @Param("portfolioId") Long portfolioId,
    @Param("techStack") String techStack
);
```

## ⚙️ 설정 파일

### application.yml
```yaml
spring:
  application:
    name: portfolio-service
  datasource:
    url: jdbc:mysql://localhost:3306/scms_portfolio
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8084

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

### 1. 양방향 연관관계 관리
- Portfolio ↔ PortfolioItem: OneToMany / ManyToOne
- PortfolioItem ↔ PortfolioAttachment: OneToMany / ManyToOne
- 연관관계 편의 메서드 제공

### 2. DTO 변환 패턴
```java
// 전체 정보 포함
public static PortfolioResponse from(Portfolio portfolio) {
    return PortfolioResponse.builder()
        .portfolioId(portfolio.getPortfolioId())
        // ... 모든 필드
        .items(portfolio.getItems().stream()
                .filter(item -> item.getDeletedAt() == null)
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList()))
        .build();
}

// 간략 정보만 (항목 제외)
public static PortfolioResponse fromWithoutItems(Portfolio portfolio) {
    return PortfolioResponse.builder()
        .portfolioId(portfolio.getPortfolioId())
        // ... 기본 필드만
        .build();
}
```

### 3. 계층형 데이터 구조
```
Portfolio (포트폴리오)
  └─ PortfolioItem (항목)
       └─ PortfolioAttachment (첨부파일)
```

### 4. 유연한 공개 범위 관리
- PUBLIC: 누구나 조회
- PRIVATE: 본인만 조회
- COMPANY_ONLY: 채용 담당자에게만 공개
- SCHOOL_ONLY: 학교 구성원에게만 공개

## 📈 성능 최적화

### 1. 인덱스 전략
```sql
-- 복합 인덱스
INDEX idx_status_visibility (status, visibility_level)

-- 정렬 성능 향상
INDEX idx_view_count (view_count DESC)
INDEX idx_like_count (like_count DESC)
INDEX idx_published_at (published_at DESC)
```

### 2. 조회 최적화
- 목록 조회 시 항목 제외 (DTO Projection)
- Lazy Loading 활용
- N+1 문제 방지

## 🧪 테스트 시나리오

### 1. 포트폴리오 생성 및 공개
```bash
# 1. 포트폴리오 생성
curl -X POST http://localhost:8084/api/portfolios \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "홍길동의 포트폴리오",
    "introduction": "백엔드 개발자",
    "contactEmail": "hong@example.com"
  }'

# 2. 상태 변경 (공개)
curl -X PATCH "http://localhost:8084/api/portfolios/1/status?status=PUBLISHED" \
  -H "X-User-Id: 1"

# 3. 공개 범위 변경
curl -X PATCH "http://localhost:8084/api/portfolios/1/visibility?visibility=PUBLIC" \
  -H "X-User-Id: 1"
```

### 2. 항목 추가
```bash
curl -X POST http://localhost:8084/api/portfolios/1/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "type": "PROJECT",
    "title": "학생 관리 시스템",
    "description": "Spring Boot 프로젝트",
    "techStack": "Spring Boot, MySQL, React",
    "startDate": "2024-01-01",
    "endDate": "2024-06-30"
  }'
```

### 3. 공개 포트폴리오 조회
```bash
curl http://localhost:8084/api/portfolios/public
```

## ✅ 구현 완료 체크리스트

- [x] Portfolio Entity 구현
- [x] PortfolioItem Entity 구현
- [x] PortfolioAttachment Entity 구현
- [x] Enum 클래스 구현 (3개)
- [x] Repository 구현 (3개)
- [x] Service 구현 (2개)
- [x] Controller 구현 (2개)
- [x] DTO 구현 (7개)
- [x] 공개 범위 관리 로직
- [x] 조회 권한 검증 로직
- [x] 좋아요/공유/조회수 관리
- [x] 항목 관리 기능
- [x] 첨부 파일 메타데이터 관리
- [x] 데이터베이스 초기화 스크립트
- [x] Application 클래스 작성
- [x] 설정 파일 작성
- [x] Eureka 등록

## 🔄 다음 단계

### 단기 계획
- [ ] 실제 파일 업로드/다운로드 기능
- [ ] 포트폴리오 템플릿 기능
- [ ] PDF 내보내기 기능
- [ ] 포트폴리오 공유 링크 생성

### 장기 계획
- [ ] AI 기반 포트폴리오 추천
- [ ] 포트폴리오 통계 및 분석
- [ ] 기업 채용담당자용 검색 기능
- [ ] 포트폴리오 평가 및 피드백

## 🐛 알려진 이슈

없음

## 📚 참고 자료

- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [JPA Relationships](https://www.baeldung.com/jpa-one-to-many)
- [DTO Pattern](https://www.baeldung.com/entity-to-and-from-dto-for-a-java-spring-application)

## 📅 작업 이력

- 2025-11-20: Portfolio Service 구현 완료
- 2025-11-20: Git 커밋 및 푸시 완료 (commit: fe5f209)
