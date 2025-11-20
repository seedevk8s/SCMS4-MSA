# Survey Service 구현 (Phase 2-5)

## 📅 작업 정보
- **작업일**: 2025-11-20
- **Phase**: 2-5
- **서비스명**: Survey Service
- **담당**: Claude
- **상태**: ✅ 완료

## 🎯 구현 목표

설문조사 관리 및 응답 수집 기능을 제공하는 마이크로서비스 구현

### 주요 기능
- 설문 CRUD (생성, 조회, 수정, 삭제)
- 다양한 질문 타입 지원 (객관식, 주관식, 평점, 척도 등)
- 질문 및 선택지 관리
- 설문 응답 수집 및 저장
- 익명 응답 지원
- 중복 응답 제어
- 응답 수정 허용 여부 설정
- 설문 공개/마감 관리
- 응답 기간 관리
- 최대 응답 수 제한
- 통계 데이터 수집

## 📊 구현 결과

### 통계
- **파일 개수**: 24개
- **코드 라인 수**: ~1,985 lines
- **API 엔드포인트**: 8개
- **포트**: 8085
- **데이터베이스**: scms_survey

### 생성된 파일 목록

#### 1. Domain Layer
```
services/survey-service/src/main/java/com/scms/survey/domain/
├── entity/
│   ├── Survey.java (240+ lines)
│   ├── Question.java (230+ lines)
│   ├── QuestionOption.java (110+ lines)
│   └── SurveyResponse.java (140+ lines)
└── enums/
    ├── SurveyType.java (8개 타입)
    ├── SurveyStatus.java (4개 상태)
    └── QuestionType.java (8개 타입)
```

#### 2. Repository Layer
```
services/survey-service/src/main/java/com/scms/survey/repository/
├── SurveyRepository.java
├── QuestionRepository.java
├── QuestionOptionRepository.java
└── SurveyResponseRepository.java
```

#### 3. Service Layer
```
services/survey-service/src/main/java/com/scms/survey/service/
└── SurveyService.java
```

#### 4. Controller Layer
```
services/survey-service/src/main/java/com/scms/survey/controller/
└── SurveyController.java
```

#### 5. DTO Layer
```
services/survey-service/src/main/java/com/scms/survey/dto/
├── request/
│   ├── SurveyCreateRequest.java
│   ├── QuestionCreateRequest.java
│   └── SurveyResponseSubmitRequest.java
└── response/
    ├── SurveyResponse.java
    ├── QuestionResponse.java
    └── QuestionOptionResponse.java
```

## 🏗️ 아키텍처 설계

### 1. Entity 설계

#### Survey Entity (설문 메인)
```java
@Entity
@Table(name = "surveys")
public class Survey {
    private Long surveyId;
    private String title;
    private String description;
    private SurveyType type;
    private SurveyStatus status;
    private Long createdBy;
    private String targetUserIds;
    private String targetGroup;
    private Boolean anonymous;
    private Boolean allowMultipleResponses;
    private Boolean allowEdit;
    private Boolean showResults;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long responseCount;
    private Long maxResponses;
    private List<Question> questions;
    // ... 기타 필드
}
```

**주요 기능**:
- 설문 정보 업데이트
- 상태 변경
- 질문 추가/제거
- 응답 수 증가
- 응답 가능 여부 확인
- 응답 기간 확인

#### Question Entity (질문)
```java
@Entity
@Table(name = "questions")
public class Question {
    private Long questionId;
    private Survey survey;
    private QuestionType type;
    private String content;
    private String description;
    private Integer displayOrder;
    private Boolean required;
    private List<QuestionOption> options;
    private Integer minValue;
    private Integer maxValue;
    private String minLabel;
    private String maxLabel;
    private Integer maxSelections;
    private Integer maxLength;
    private String allowedFileExtensions;
    private Long maxFileSize;
    // ... 기타 필드
}
```

**주요 기능**:
- 질문 내용 수정
- 선택지 추가/제거
- 순서 변경
- 평점/척도 설정
- 파일 업로드 설정
- 객관식/주관식 여부 확인

#### QuestionOption Entity (선택지)
```java
@Entity
@Table(name = "question_options")
public class QuestionOption {
    private Long optionId;
    private Question question;
    private String content;
    private Integer displayOrder;
    private Boolean allowOtherInput;
    private Long selectionCount;
    // ... 기타 필드
}
```

**주요 기능**:
- 선택지 내용 수정
- 선택 횟수 증가
- 순서 변경

#### SurveyResponse Entity (응답)
```java
@Entity
@Table(name = "survey_responses")
public class SurveyResponse {
    private Long responseId;
    private Long surveyId;
    private Long questionId;
    private Long userId;
    private String selectedOptionIds;
    private String textAnswer;
    private Integer numberAnswer;
    private LocalDateTime dateAnswer;
    private String fileUrl;
    private String fileName;
    private String sessionId;
    private String ipAddress;
    // ... 기타 필드
}
```

**주요 기능**:
- 텍스트 응답 업데이트
- 선택지 응답 업데이트
- 숫자 응답 업데이트
- 날짜 응답 업데이트
- 파일 응답 업데이트

### 2. Enum 설계

#### SurveyType (8가지)
```java
public enum SurveyType {
    SATISFACTION("만족도 조사"),
    NEEDS_ASSESSMENT("수요 조사"),
    EMPLOYMENT("취업 현황"),
    CAREER("진로 조사"),
    PROGRAM_EVALUATION("프로그램 평가"),
    EVENT_FEEDBACK("행사 피드백"),
    GENERAL("일반 설문"),
    OTHER("기타");
}
```

#### SurveyStatus (4가지)
```java
public enum SurveyStatus {
    DRAFT("임시 저장"),
    PUBLISHED("공개"),
    CLOSED("마감"),
    ARCHIVED("보관");
}
```

#### QuestionType (8가지)
```java
public enum QuestionType {
    SINGLE_CHOICE("객관식 (단일 선택)"),
    MULTIPLE_CHOICE("객관식 (복수 선택)"),
    SHORT_ANSWER("단답형"),
    LONG_ANSWER("서술형"),
    RATING("평점"),
    SCALE("척도"),
    DATE("날짜"),
    FILE_UPLOAD("파일 첨부");
}
```

## 🔌 API 설계

### 1. Survey API (8개 엔드포인트)

#### 설문 생성
```http
POST /api/surveys
Content-Type: application/json
X-User-Id: {userId}

{
  "title": "2024년 프로그램 만족도 조사",
  "description": "프로그램 개선을 위한 설문",
  "type": "SATISFACTION",
  "anonymous": false,
  "allowMultipleResponses": false,
  "allowEdit": true,
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-01-31T23:59:59",
  "maxResponses": 100
}
```

#### 질문 추가
```http
POST /api/surveys/{surveyId}/questions
Content-Type: application/json
X-User-Id: {userId}

{
  "type": "SINGLE_CHOICE",
  "content": "프로그램에 만족하셨나요?",
  "description": "솔직한 의견 부탁드립니다",
  "required": true,
  "displayOrder": 1,
  "options": [
    {"content": "매우 만족", "displayOrder": 1},
    {"content": "만족", "displayOrder": 2},
    {"content": "보통", "displayOrder": 3},
    {"content": "불만족", "displayOrder": 4},
    {"content": "매우 불만족", "displayOrder": 5}
  ]
}
```

#### 설문 응답 제출
```http
POST /api/surveys/{surveyId}/responses
Content-Type: application/json
X-User-Id: {userId}

{
  "answers": [
    {
      "questionId": 1,
      "selectedOptionIds": [2]
    },
    {
      "questionId": 2,
      "textAnswer": "프로그램이 유익했습니다"
    },
    {
      "questionId": 3,
      "numberAnswer": 5
    }
  ]
}
```

#### 설문 목록 조회
```http
GET /api/surveys
```

#### 응답 가능한 설문 목록
```http
GET /api/surveys/available
```

#### 설문 상세 조회
```http
GET /api/surveys/{surveyId}
```

#### 설문 상태 변경
```http
PATCH /api/surveys/{surveyId}/status?status=PUBLISHED
X-User-Id: {userId}
```

#### 설문 삭제
```http
DELETE /api/surveys/{surveyId}
X-User-Id: {userId}
```

## 💾 데이터베이스 설계

### 테이블 구조

#### surveys 테이블
```sql
CREATE TABLE surveys (
    survey_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    target_user_ids TEXT,
    target_group VARCHAR(100),
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    allow_multiple_responses BOOLEAN NOT NULL DEFAULT FALSE,
    allow_edit BOOLEAN NOT NULL DEFAULT TRUE,
    show_results BOOLEAN NOT NULL DEFAULT FALSE,
    start_date DATETIME,
    end_date DATETIME,
    response_count BIGINT NOT NULL DEFAULT 0,
    max_responses BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date)
);
```

#### questions 테이블
```sql
CREATE TABLE questions (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    min_value INT,
    max_value INT,
    min_label VARCHAR(100),
    max_label VARCHAR(100),
    max_selections INT,
    max_length INT,
    allowed_file_extensions VARCHAR(200),
    max_file_size BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_survey_id (survey_id),
    INDEX idx_display_order (display_order)
);
```

#### question_options 테이블
```sql
CREATE TABLE question_options (
    option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    allow_other_input BOOLEAN NOT NULL DEFAULT FALSE,
    selection_count BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_question_id (question_id),
    INDEX idx_selection_count (selection_count DESC)
);
```

#### survey_responses 테이블
```sql
CREATE TABLE survey_responses (
    response_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_id BIGINT,
    selected_option_ids VARCHAR(500),
    text_answer TEXT,
    number_answer INT,
    date_answer DATETIME,
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    session_id VARCHAR(100),
    ip_address VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_survey_user (survey_id, user_id),
    INDEX idx_question_user (question_id, user_id),
    INDEX idx_session (survey_id, session_id)
);
```

## 🔧 주요 비즈니스 로직

### 1. 응답 가능 여부 확인

```java
public boolean isAvailableForResponse() {
    if (this.status != SurveyStatus.PUBLISHED) {
        return false;
    }

    LocalDateTime now = LocalDateTime.now();
    if (this.startDate != null && now.isBefore(this.startDate)) {
        return false;
    }

    if (this.endDate != null && now.isAfter(this.endDate)) {
        return false;
    }

    if (this.maxResponses != null && this.responseCount >= this.maxResponses) {
        return false;
    }

    return true;
}
```

### 2. 응답 제출 및 검증

```java
@Transactional
public void submitResponse(Long surveyId, SurveyResponseSubmitRequest request, Long userId) {
    Survey survey = getSurveyEntity(surveyId);

    // 응답 가능 여부 확인
    if (!survey.isAvailableForResponse()) {
        throw new ApiException(ErrorCode.SURVEY_CLOSED, "현재 응답할 수 없는 설문입니다.");
    }

    // 중복 응답 확인
    if (!survey.getAllowMultipleResponses()) {
        if (userId != null &&
            surveyResponseRepository.existsBySurveyIdAndUserIdAndDeletedAtIsNull(surveyId, userId)) {
            throw new ApiException(ErrorCode.SURVEY_ALREADY_SUBMITTED, "이미 응답한 설문입니다.");
        }
    }

    // 응답 저장 및 통계 업데이트
    for (SurveyResponseSubmitRequest.Answer answer : request.getAnswers()) {
        // 응답 저장
        saveAnswer(surveyId, answer, userId, request.getSessionId());

        // 선택지 통계 업데이트
        if (answer.getSelectedOptionIds() != null) {
            for (Long optionId : answer.getSelectedOptionIds()) {
                questionOptionRepository.incrementSelectionCount(optionId);
            }
        }
    }

    // 응답 수 증가
    surveyRepository.incrementResponseCount(surveyId);
}
```

### 3. 익명 응답 처리

```java
// 익명 응답인 경우 userId는 null, sessionId로 추적
SurveyResponse response = SurveyResponse.builder()
    .surveyId(surveyId)
    .questionId(answer.getQuestionId())
    .userId(survey.getAnonymous() ? null : userId)
    .sessionId(request.getSessionId())
    // ... 기타 필드
    .build();
```

## 📝 Repository 쿼리 메서드

### 복잡한 쿼리 예시

#### 응답 가능한 설문 조회
```java
@Query("SELECT s FROM Survey s WHERE s.status = 'PUBLISHED' " +
        "AND (s.startDate IS NULL OR s.startDate <= :now) " +
        "AND (s.endDate IS NULL OR s.endDate >= :now) " +
        "AND (s.maxResponses IS NULL OR s.responseCount < s.maxResponses) " +
        "AND s.deletedAt IS NULL " +
        "ORDER BY s.createdAt DESC")
List<Survey> findAvailableSurveys(@Param("now") LocalDateTime now);
```

#### 마감 임박 설문 조회
```java
@Query("SELECT s FROM Survey s WHERE s.status = 'PUBLISHED' " +
        "AND s.endDate IS NOT NULL " +
        "AND s.endDate BETWEEN :now AND :deadline " +
        "AND s.deletedAt IS NULL " +
        "ORDER BY s.endDate ASC")
List<Survey> findClosingSoonSurveys(
    @Param("now") LocalDateTime now,
    @Param("deadline") LocalDateTime deadline
);
```

#### 특정 옵션 선택 횟수 조회
```java
@Query("SELECT COUNT(r) FROM SurveyResponse r WHERE r.selectedOptionIds LIKE %:optionId% " +
        "AND r.deletedAt IS NULL")
long countBySelectedOption(@Param("optionId") String optionId);
```

## ⚙️ 설정 파일

### application.yml
```yaml
spring:
  application:
    name: survey-service
  datasource:
    url: jdbc:mysql://localhost:3306/scms_survey
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8085

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

### 1. 다양한 질문 타입 지원
- **객관식**: 단일 선택, 복수 선택
- **주관식**: 단답형, 서술형
- **평가형**: 평점, 척도
- **기타**: 날짜, 파일 첨부

### 2. 유연한 응답 관리
- 익명 응답 지원
- 중복 응답 제어
- 응답 수정 허용
- 세션 기반 추적

### 3. 통계 데이터 수집
- 선택지별 선택 횟수
- 응답 수 집계
- 실시간 통계 업데이트

### 4. 응답 기간 관리
- 시작일/종료일 설정
- 자동 마감 처리
- 마감 임박 알림

## 📈 성능 최적화

### 1. 인덱스 전략
```sql
-- 복합 인덱스
INDEX idx_survey_user (survey_id, user_id)
INDEX idx_question_user (question_id, user_id)
INDEX idx_session (survey_id, session_id)

-- 통계 조회 성능 향상
INDEX idx_selection_count (selection_count DESC)
```

### 2. 응답 저장 최적화
- Batch Insert 가능
- 트랜잭션 단위 조정
- 통계 업데이트 비동기 처리 준비

## 🧪 테스트 시나리오

### 1. 설문 생성 및 질문 추가
```bash
# 1. 설문 생성
curl -X POST http://localhost:8085/api/surveys \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "만족도 조사",
    "type": "SATISFACTION",
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-01-31T23:59:59"
  }'

# 2. 질문 추가
curl -X POST http://localhost:8085/api/surveys/1/questions \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "type": "SINGLE_CHOICE",
    "content": "만족도를 선택하세요",
    "required": true,
    "options": [
      {"content": "매우 만족", "displayOrder": 1},
      {"content": "만족", "displayOrder": 2}
    ]
  }'

# 3. 상태 변경 (공개)
curl -X PATCH "http://localhost:8085/api/surveys/1/status?status=PUBLISHED" \
  -H "X-User-Id: 1"
```

### 2. 응답 제출
```bash
curl -X POST http://localhost:8085/api/surveys/1/responses \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{
    "answers": [
      {
        "questionId": 1,
        "selectedOptionIds": [1]
      }
    ]
  }'
```

### 3. 응답 가능한 설문 조회
```bash
curl http://localhost:8085/api/surveys/available
```

## ✅ 구현 완료 체크리스트

- [x] Survey Entity 구현
- [x] Question Entity 구현
- [x] QuestionOption Entity 구현
- [x] SurveyResponse Entity 구현
- [x] Enum 클래스 구현 (3개)
- [x] Repository 구현 (4개)
- [x] Service 구현
- [x] Controller 구현
- [x] DTO 구현 (6개)
- [x] 응답 가능 여부 검증 로직
- [x] 중복 응답 제어
- [x] 익명 응답 처리
- [x] 통계 수집 기능
- [x] 데이터베이스 초기화 스크립트
- [x] Application 클래스 작성
- [x] 설정 파일 작성
- [x] Eureka 등록

## 🔄 다음 단계

### 단기 계획
- [ ] 설문 결과 통계 API
- [ ] Excel/CSV 내보내기 기능
- [ ] 차트/그래프 데이터 제공
- [ ] 설문 복제 기능

### 장기 계획
- [ ] 조건부 질문 (스킵 로직)
- [ ] 설문 템플릿 제공
- [ ] AI 기반 질문 추천
- [ ] 실시간 응답 모니터링
- [ ] 알림 서비스 연동 (Notification Service)

## 🐛 알려진 이슈

없음

## 📚 참고 자료

- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Survey Design Best Practices](https://www.questionpro.com/blog/survey-design/)
- [Database Indexing Strategies](https://use-the-index-luke.com/)

## 📅 작업 이력

- 2025-11-20: Survey Service 구현 완료
- 2025-11-20: Git 커밋 및 푸시 완료 (commit: 0619f54)
