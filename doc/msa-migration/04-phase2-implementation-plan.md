# Phase 2: 전체 기능 구현 계획

**작성일**: 2025-11-19
**목표**: 기존 모노리틱의 모든 기능을 MSA로 완전히 구현

---

## 📋 현재 상태

### ✅ 완료된 작업 (Phase 1)
- 인프라 구축 완료 (Eureka, API Gateway, Config Server)
- 공통 라이브러리 구축 완료
- User Service, Notification Service 구조 완성
- 빌드 및 실행 검증 완료

### 🔄 남은 작업
- **2개 서비스**: 구조만 있음 (Entity, Repository, Service, Controller 미구현)
- **8개 서비스**: 구조 자체가 없음
- **프론트엔드**: Thymeleaf 템플릿 마이그레이션 필요

---

## 🏗 서비스 분할 전략

### 모노리틱 분석 결과

기존 모노리틱 시스템은 다음과 같은 구성:
- **47개 Entity**
- **30+ Controller**
- **23개 Service**
- **기술 스택**: Spring Security, OAuth2, Thymeleaf, JPA, MySQL, Apache POI

### MSA 서비스 매핑

| 서비스명 | Entity 수 | Controller 수 | 복잡도 | 우선순위 |
|---------|----------|--------------|--------|----------|
| **User Service** | 7개 | 2개 | 높음 | 1 |
| **Notification Service** | 1개 | 1개 | 중간 | 2 |
| **Program Service** | 5개 | 4개 | 높음 | 3 |
| **Program Application Service** | 1개 | 1개 | 중간 | 4 |
| **Portfolio Service** | 5개 | 3개 | 높음 | 5 |
| **Survey Service** | 6개 | 2개 | 높음 | 6 |
| **Consultation Service** | 3개 | 2개 | 중간 | 7 |
| **Competency Service** | 3개 | 3개 | 중간 | 8 |
| **Mileage Service** | 2개 | 2개 | 낮음 | 9 |
| **External Employment Service** | 2개 | 2개 | 낮음 | 10 |

---

## 📅 Phase 2 세부 일정

### Phase 2-1: User Service 완전 구현 (예상 4-6시간)

**목표**: 인증/인가의 핵심이 되는 User Service를 완전히 구현

#### Entity (7개)
- [x] ~~User~~ (구조 설계)
- [ ] Student
- [ ] ExternalUser
- [ ] Counselor
- [ ] LoginHistory
- [ ] PasswordResetToken
- [ ] Enum 클래스들 (UserRole, UserType, Gender, AccountStatus)

#### Repository (7개)
- [ ] UserRepository
- [ ] StudentRepository
- [ ] ExternalUserRepository
- [ ] CounselorRepository
- [ ] LoginHistoryRepository
- [ ] PasswordResetTokenRepository

#### Service (5개)
- [ ] UserService
- [ ] AuthService
- [ ] StudentService
- [ ] ExternalUserService
- [ ] CounselorService
- [ ] EmailService (비밀번호 재설정용)

#### Controller (2개)
- [ ] AuthController (8개 엔드포인트)
- [ ] UserController (7개 엔드포인트)

#### Security 설정
- [ ] Spring Security 설정
- [ ] JWT 인증 필터
- [ ] OAuth2 설정 (Google, Kakao)
- [ ] Password Encoder
- [ ] Custom UserDetailsService

#### 테스트
- [ ] 로그인/로그아웃 테스트
- [ ] 비밀번호 변경/재설정 테스트
- [ ] 사용자 CRUD 테스트

---

### Phase 2-2: Notification Service 완전 구현 (예상 2-3시간)

**목표**: 알림 시스템을 완전히 구현하여 다른 서비스와 연동

#### Entity (1개)
- [ ] Notification
- [ ] Enum (NotificationType)

#### Repository (1개)
- [ ] NotificationRepository

#### Service (2개)
- [ ] NotificationService
- [ ] EmailService (이메일 알림)

#### Controller (1개)
- [ ] NotificationController (7개 엔드포인트)

#### RabbitMQ 통합
- [ ] RabbitMQ 리스너 설정
- [ ] 이벤트 메시지 수신 및 처리
- [ ] 알림 자동 생성

#### 스케줄러
- [ ] 알림 스케줄링 (@Scheduled)
- [ ] 배치 알림 발송

---

### Phase 2-3: Program Service 구현 (예상 6-8시간)

**목표**: 핵심 비즈니스인 비교과 프로그램 관리

#### Entity (5개)
- [ ] Program
- [ ] ProgramReview
- [ ] ProgramFile
- [ ] ProgramCompetency
- [ ] Enum (ProgramStatus)

#### Repository (5개)
- [ ] ProgramRepository
- [ ] ProgramReviewRepository
- [ ] ProgramFileRepository
- [ ] ProgramCompetencyRepository

#### Service (4개)
- [ ] ProgramService
- [ ] ProgramReviewService
- [ ] ProgramFileService
- [ ] ProgramRecommendationService

#### Controller (3개)
- [ ] ProgramController (CRUD)
- [ ] ProgramReviewController
- [ ] ProgramFileController
- [ ] ProgramRecommendationController

#### 파일 관리
- [ ] 파일 업로드/다운로드
- [ ] 파일 메타데이터 저장

---

### Phase 2-4: Program Application Service 구현 (예상 3-4시간)

**목표**: 프로그램 신청 관리

#### Entity (1개)
- [ ] ProgramApplication
- [ ] Enum (ApplicationStatus)

#### Repository (1개)
- [ ] ProgramApplicationRepository

#### Service (2개)
- [ ] ProgramApplicationService
- [ ] ExcelService (Excel 다운로드)

#### Controller (1개)
- [ ] ProgramApplicationController (11개 엔드포인트)

#### 통합
- [ ] Program Service와 연동
- [ ] Notification Service와 이벤트 연동 (RabbitMQ)
- [ ] Mileage Service와 연동 (완료 시 마일리지 지급)

---

### Phase 2-5: Portfolio Service 구현 (예상 5-6시간)

**목표**: 포트폴리오 관리 시스템

#### Entity (5개)
- [ ] Portfolio
- [ ] PortfolioItem
- [ ] PortfolioFile
- [ ] PortfolioShare
- [ ] PortfolioView
- [ ] Enum (PortfolioVisibility, PortfolioItemType)

#### Repository (5개)
- [ ] PortfolioRepository
- [ ] PortfolioItemRepository
- [ ] PortfolioFileRepository
- [ ] PortfolioShareRepository
- [ ] PortfolioViewRepository

#### Service (3개)
- [ ] PortfolioService
- [ ] PortfolioItemService
- [ ] PortfolioFileService

#### Controller (3개)
- [ ] PortfolioController (9개 엔드포인트)
- [ ] PortfolioItemController
- [ ] PortfolioFileController

#### 고급 기능
- [ ] 공유 링크 생성 (UUID)
- [ ] 조회 이력 추적
- [ ] 공개 범위 설정

---

### Phase 2-6: Survey Service 구현 (예상 5-6시간)

**목표**: 설문조사 시스템

#### Entity (6개)
- [ ] Survey
- [ ] SurveyQuestion
- [ ] SurveyQuestionOption
- [ ] SurveyAnswer
- [ ] SurveyResponse
- [ ] SurveyTarget
- [ ] Enum (QuestionType, SurveyTargetType)

#### Repository (6개)
- [ ] SurveyRepository
- [ ] SurveyQuestionRepository
- [ ] SurveyQuestionOptionRepository
- [ ] SurveyAnswerRepository
- [ ] SurveyResponseRepository
- [ ] SurveyTargetRepository

#### Service (2개)
- [ ] SurveyService
- [ ] SurveyResponseService

#### Controller (1개)
- [ ] SurveyController (12개 엔드포인트)

#### 통계 기능
- [ ] 설문 응답 통계
- [ ] 결과 집계 및 분석

---

### Phase 2-7: Consultation Service 구현 (예상 4-5시간)

**목표**: 상담 신청 및 관리

#### Entity (3개)
- [ ] ConsultationSession
- [ ] ConsultationRecord
- [ ] CounselorSchedule
- [ ] Enum (ConsultationType, ConsultationStatus)

#### Repository (3개)
- [ ] ConsultationSessionRepository
- [ ] ConsultationRecordRepository
- [ ] CounselorScheduleRepository

#### Service (3개)
- [ ] ConsultationService
- [ ] ConsultationRecordService
- [ ] CounselorService

#### Controller (2개)
- [ ] ConsultationController
- [ ] CounselorController

#### 통합
- [ ] User Service (Counselor 정보)
- [ ] Notification Service (상담 알림)

---

### Phase 2-8: Competency Service 구현 (예상 3-4시간)

**목표**: 역량 관리 및 평가

#### Entity (3개)
- [ ] CompetencyCategory
- [ ] Competency
- [ ] StudentCompetencyAssessment

#### Repository (3개)
- [ ] CompetencyCategoryRepository
- [ ] CompetencyRepository
- [ ] StudentCompetencyAssessmentRepository

#### Service (2개)
- [ ] CompetencyService
- [ ] CompetencyAssessmentService

#### Controller (2개)
- [ ] CompetencyController
- [ ] CompetencyAssessmentController

#### 통합
- [ ] Program Service (프로그램-역량 매핑)
- [ ] User Service (학생 정보)

---

### Phase 2-9: Mileage Service 구현 (예상 2-3시간)

**목표**: 마일리지 적립 및 관리

#### Entity (2개)
- [ ] MileageHistory
- [ ] MileageRule

#### Repository (2개)
- [ ] MileageHistoryRepository
- [ ] MileageRuleRepository

#### Service (1개)
- [ ] MileageService

#### Controller (1개)
- [ ] MileageController

#### 통합
- [ ] Program Application Service (완료 시 지급)
- [ ] Survey Service (응답 시 지급)

---

### Phase 2-10: External Employment Service 구현 (예상 2-3시간)

**목표**: 외부 취업 활동 관리

#### Entity (2개)
- [ ] ExternalEmployment
- [ ] ExternalEmploymentCreditRule
- [ ] Enum (EmploymentType)

#### Repository (2개)
- [ ] ExternalEmploymentRepository
- [ ] ExternalEmploymentCreditRuleRepository

#### Service (1개)
- [ ] ExternalEmploymentService

#### Controller (1개)
- [ ] ExternalEmploymentController

#### 통합
- [ ] Portfolio Service (포트폴리오 연동)
- [ ] User Service (학생 정보)

---

## 🎯 통합 작업

### API Gateway 업데이트
- [ ] JWT 검증 필터 추가
- [ ] 서비스별 라우팅 규칙 세부 조정
- [ ] CORS 정책 업데이트

### 서비스 간 통신
- [ ] RabbitMQ 이벤트 정의
  - ProgramApplicationApproved
  - ProgramApplicationCompleted
  - SurveySubmitted
  - ConsultationBooked
- [ ] 이벤트 발행자/구독자 구현

### 데이터베이스
- [ ] 서비스별 독립 데이터베이스 생성
  - scms_user
  - scms_notification
  - scms_program
  - scms_application
  - scms_portfolio
  - scms_survey
  - scms_consultation
  - scms_competency
  - scms_mileage
  - scms_employment

---

## 🖥 프론트엔드 고려사항

### 현재 상황
- 기존: Thymeleaf 서버 사이드 렌더링
- MSA: API 기반 아키텍처

### 옵션 1: Thymeleaf 유지 (단기)
- API Gateway를 통해 API 호출
- Thymeleaf를 별도 Frontend 서비스로 분리
- 세션 기반 인증 유지

### 옵션 2: SPA 전환 (장기)
- React/Vue.js로 전환
- JWT 기반 인증으로 변경
- REST API 완전 활용

### 권장 접근
1. **Phase 2**: Thymeleaf 유지, API만 MSA로 분리
2. **Phase 3**: 점진적으로 SPA로 전환

---

## 📊 예상 일정

| Phase | 작업 내용 | 예상 시간 | 누적 시간 |
|-------|----------|-----------|----------|
| **2-1** | User Service 구현 | 6시간 | 6시간 |
| **2-2** | Notification Service 구현 | 3시간 | 9시간 |
| **2-3** | Program Service 구현 | 8시간 | 17시간 |
| **2-4** | Program Application Service | 4시간 | 21시간 |
| **2-5** | Portfolio Service | 6시간 | 27시간 |
| **2-6** | Survey Service | 6시간 | 33시간 |
| **2-7** | Consultation Service | 5시간 | 38시간 |
| **2-8** | Competency Service | 4시간 | 42시간 |
| **2-9** | Mileage Service | 3시간 | 45시간 |
| **2-10** | External Employment Service | 3시간 | 48시간 |
| **통합** | 서비스 간 통합 및 테스트 | 8시간 | 56시간 |
| **문서** | 문서 작성 및 정리 | 4시간 | **60시간** |

**총 예상 시간**: 약 60시간 (7.5일, 하루 8시간 기준)

---

## 🎓 구현 원칙

### 1. Database Per Service
- 각 서비스가 독립적인 데이터베이스 소유
- 다른 서비스 DB에 직접 접근 금지
- API 또는 이벤트를 통한 데이터 공유

### 2. Domain-Driven Design
- 명확한 도메인 경계 설정
- Bounded Context 존중
- Aggregate Root 중심 설계

### 3. API First
- API 명세 우선 작성
- OpenAPI/Swagger 문서화
- 버전 관리 (v1, v2)

### 4. Event-Driven Architecture
- 동기 호출 최소화
- RabbitMQ를 통한 느슨한 결합
- 이벤트 소싱 고려

### 5. 보안
- JWT 기반 인증
- API Gateway에서 통합 인증
- 서비스별 권한 체크

### 6. 테스트
- 단위 테스트 (Service 레이어)
- 통합 테스트 (Controller)
- E2E 테스트 (주요 시나리오)

---

## 📚 문서 산출물

각 Phase마다 다음 문서 작성:

1. **서비스 설계 문서** (`XX-service-design.md`)
   - Entity 상세 설계
   - API 명세
   - 이벤트 정의

2. **구현 로그** (`XX-service-implementation.md`)
   - 구현 과정
   - 발생한 문제 및 해결
   - 테스트 결과

3. **API 문서** (Swagger/OpenAPI)
   - 자동 생성
   - 엔드포인트별 상세 설명

---

## 🚀 시작 지점

**다음 작업**: Phase 2-1 - User Service 완전 구현

1. Entity 클래스 작성
2. Repository 작성
3. Service 구현
4. Controller 구현
5. Security 설정
6. 테스트 작성
7. 문서 작성

---

**작성일**: 2025-11-19
**다음 문서**: `05-user-service-implementation.md`
