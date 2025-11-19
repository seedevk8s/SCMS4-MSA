# MSA 마이그레이션 계획

**작성일:** 2025-11-19
**작성자:** Claude
**프로젝트:** SCMS4-MSA (모노리틱 → 마이크로서비스)

---

## 1. 개요

### 1.1 목적
SCMS4 학생 역량 관리 시스템을 모노리틱 아키텍처에서 마이크로서비스 아키텍처(MSA)로 전환하여:
- 서비스별 독립적인 배포 및 확장성 확보
- 장애 격리를 통한 시스템 안정성 향상
- 도메인별 기술 스택 최적화 가능
- 팀별 독립적인 개발 및 운영 지원

### 1.2 현재 시스템 구조
- **아키텍처:** Spring Boot 모노리틱
- **기술 스택:** Spring Boot 3.3.0, Java 17, MySQL 8.0, JPA
- **주요 기능:** 사용자 관리, 프로그램 관리, 포트폴리오, 상담, 역량 평가, 마일리지, 설문조사
- **컨트롤러:** 32개
- **서비스:** 23개
- **리포지토리:** 33개
- **엔티티:** 47개

---

## 2. 마이크로서비스 분할 전략

### 2.1 식별된 마이크로서비스 (10개)

| 순번 | 서비스명 | 주요 책임 | 우선순위 |
|------|---------|---------|---------|
| 1 | **User Service** | 사용자 인증/인가, 계정 관리, OAuth2 | 최우선 |
| 2 | **Notification Service** | 시스템 알림, 이메일 발송 | 최우선 |
| 3 | **Program Service** | 비교과 프로그램 CRUD, 조회 | 높음 |
| 4 | **Program Application Service** | 프로그램 신청, 심사, 리뷰 | 높음 |
| 5 | **Portfolio Service** | 포트폴리오 관리, 파일 업로드 | 중간 |
| 6 | **Consultation Service** | 상담 예약, 상담 기록 관리 | 중간 |
| 7 | **Competency Service** | 역량 관리, 평가, 리포트 | 중간 |
| 8 | **Mileage Service** | 마일리지 적립/사용, 규칙 관리 | 낮음 |
| 9 | **Survey Service** | 설문 관리, 응답 수집, 통계 | 낮음 |
| 10 | **External Employment Service** | 외부 고용 정보 관리 | 낮음 |

### 2.2 서비스 간 의존성 분석

```
User Service (중심)
├── 모든 서비스가 사용자 정보 참조
└── 인증 토큰 제공

Program Service
├── Competency Service (프로그램-역량 매핑)
└── Notification Service (프로그램 알림)

Program Application Service
├── User Service (신청자 정보)
├── Program Service (프로그램 정보)
├── Mileage Service (참가 포인트 적립)
└── Notification Service (신청 결과 알림)

Portfolio Service
├── User Service (소유자 정보)
├── Program Application Service (자동 가져오기)
└── Notification Service (공유 알림)

Consultation Service
├── User Service (학생/상담사 정보)
├── Mileage Service (상담 포인트)
└── Notification Service (예약 알림)

Mileage Service
├── User Service (사용자별 마일리지)
├── Program Application Service (이벤트 구독)
├── Consultation Service (이벤트 구독)
└── External Employment Service (이벤트 구독)

Survey Service
├── User Service (응답자 정보)
└── Notification Service (설문 요청 알림)

Competency Service
├── User Service (학생 정보)
└── Program Service (프로그램-역량 매핑)

External Employment Service
├── User Service (외부 회원)
└── Mileage Service (고용 정보 등록 포인트)
```

---

## 3. 아키텍처 설계

### 3.1 전체 구조도

```
┌─────────────────────────────────────────────────────────────┐
│                         Client Layer                         │
│                    (Web Browser / Mobile)                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                     API Gateway                              │
│              (Spring Cloud Gateway)                          │
│        - 라우팅, 인증, Rate Limiting, CORS                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                 Service Discovery                            │
│                  (Eureka Server)                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
┌────────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
│ User Service  │  │   Program   │  │  Portfolio  │
│   (MySQL)     │  │   Service   │  │   Service   │
└───────┬───────┘  │   (MySQL)   │  │   (MySQL)   │
        │          └──────┬──────┘  └──────┬──────┘
        │                 │                 │
┌───────▼──────────────────▼─────────────────▼──────┐
│           Message Broker (RabbitMQ)               │
│         - Event-Driven Communication              │
└───────┬──────────────────┬─────────────────┬──────┘
        │                  │                 │
┌───────▼──────┐  ┌────────▼──────┐  ┌──────▼──────┐
│ Notification │  │   Mileage     │  │  Survey     │
│   Service    │  │   Service     │  │  Service    │
│   (MySQL)    │  │   (MySQL)     │  │  (MySQL)    │
└──────────────┘  └───────────────┘  └─────────────┘

┌──────────────────────────────────────────────────────────────┐
│              Config Server (Spring Cloud Config)             │
│              - 중앙 집중식 설정 관리                           │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│           Monitoring & Logging (선택 사항)                    │
│   - Zipkin (분산 추적)                                        │
│   - Prometheus + Grafana (메트릭)                            │
│   - ELK Stack (로그 집계)                                     │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 통신 패턴

| 패턴 | 사용 사례 | 기술 스택 |
|------|----------|----------|
| **동기 REST** | 사용자 인증, 실시간 데이터 조회 | Spring WebClient, OpenFeign |
| **비동기 이벤트** | 알림 발송, 마일리지 적립, 통계 업데이트 | RabbitMQ, Spring AMQP |
| **API Gateway 라우팅** | 클라이언트 → 서비스 요청 | Spring Cloud Gateway |

### 3.3 데이터 관리 전략

#### Database Per Service 패턴
- 각 마이크로서비스는 독립적인 데이터베이스 보유
- 서비스 간 직접적인 데이터베이스 접근 금지
- API 또는 이벤트를 통해서만 데이터 공유

#### 데이터 일관성
- **강한 일관성:** User 인증, Program 예약 (Saga 패턴 고려)
- **최종 일관성:** Notification, Mileage 적립, 통계 집계

---

## 4. 마이그레이션 로드맵

### Phase 0: 사전 준비 (1주)
- [ ] 프로젝트 구조 분석 완료 ✅
- [ ] MSA 아키텍처 설계 완료 ✅
- [ ] 마이그레이션 계획 수립 ✅
- [ ] 개발 환경 준비

### Phase 1: 인프라 구축 (2-3주)
- [ ] 공통 라이브러리 모듈 생성
- [ ] Eureka Server 구성
- [ ] API Gateway 구성
- [ ] Config Server 구성
- [ ] RabbitMQ 설정

**예상 산출물:**
- `common-library/` - 공통 DTO, 예외, 유틸리티
- `eureka-server/` - 서비스 디스커버리
- `api-gateway/` - API 게이트웨이
- `config-server/` - 중앙 설정 서버
- `docker-compose.yml` - 인프라 환경

### Phase 2: 핵심 서비스 분리 (3-4주)
- [ ] User Service 분리
  - User, LoginHistory, PasswordResetToken, ExternalUser 마이그레이션
  - JWT 기반 인증 구현
  - OAuth2 통합 (Google, Kakao, Naver)

- [ ] Notification Service 분리
  - Notification 엔티티 마이그레이션
  - 이메일 서비스 통합
  - RabbitMQ 이벤트 리스너 구현

**예상 산출물:**
- `user-service/`
- `notification-service/`
- 개발 로그: `01-phase1-infrastructure.md`, `02-phase2-core-services.md`

### Phase 3: 비즈니스 서비스 분리 (6-8주)
- [ ] Program Service
  - Program, ProgramFile, ProgramCompetency 마이그레이션
  - 파일 업로드 처리

- [ ] Program Application Service
  - ProgramApplication, ProgramReview 마이그레이션
  - 신청/심사 워크플로우
  - Mileage Service와 이벤트 통합

- [ ] Portfolio Service
  - Portfolio, PortfolioItem, PortfolioFile 마이그레이션
  - 파일 저장소 통합

- [ ] Consultation Service
  - ConsultationSession, ConsultationRecord, Counselor 마이그레이션
  - 예약 시스템

- [ ] Competency Service
  - Competency, CompetencyCategory, StudentCompetencyAssessment 마이그레이션
  - 평가 리포트 생성

**예상 산출물:**
- `program-service/`
- `program-application-service/`
- `portfolio-service/`
- `consultation-service/`
- `competency-service/`
- 개발 로그: `03-phase3-business-services.md`

### Phase 4: 나머지 서비스 분리 (3-4주)
- [ ] Mileage Service
  - MileageHistory, MileageRule 마이그레이션
  - 이벤트 기반 포인트 적립/차감
  - 분산 트랜잭션 처리 (Saga 패턴)

- [ ] Survey Service
  - Survey, SurveyQuestion, SurveyResponse 마이그레이션
  - 설문 응답 집계

- [ ] External Employment Service
  - ExternalEmployment 마이그레이션

**예상 산출물:**
- `mileage-service/`
- `survey-service/`
- `external-employment-service/`
- 개발 로그: `04-phase4-remaining-services.md`

### Phase 5: 통합 및 최적화 (2-3주)
- [ ] Docker Compose 통합 테스트
- [ ] API 문서 자동화 (Swagger/OpenAPI)
- [ ] 모니터링 대시보드 구성
- [ ] 성능 테스트 및 최적화
- [ ] 배포 파이프라인 구축

**예상 산출물:**
- `docker-compose.yml` (전체 서비스)
- Kubernetes manifest (선택)
- 개발 로그: `05-integration-testing.md`

---

## 5. 기술 스택

### 5.1 기존 스택 유지
- **언어:** Java 17
- **프레임워크:** Spring Boot 3.3.0
- **ORM:** JPA/Hibernate
- **데이터베이스:** MySQL 8.0
- **빌드 도구:** Gradle
- **인증:** Spring Security, OAuth2

### 5.2 추가 기술
- **서비스 디스커버리:** Spring Cloud Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **설정 관리:** Spring Cloud Config
- **메시지 브로커:** RabbitMQ (또는 Kafka)
- **통신:** OpenFeign, Spring WebClient
- **회복탄력성:** Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- **컨테이너:** Docker, Docker Compose
- **오케스트레이션 (선택):** Kubernetes

### 5.3 모니터링 및 로깅 (선택)
- **분산 추적:** Spring Cloud Sleuth + Zipkin
- **메트릭:** Micrometer + Prometheus + Grafana
- **로그 집계:** ELK Stack (Elasticsearch, Logstash, Kibana)

---

## 6. 디렉토리 구조

### 6.1 최종 프로젝트 구조
```
SCMS4-MSA/
├── common-library/              # 공통 라이브러리
│   ├── common-dto/              # 공통 DTO
│   ├── common-exception/        # 공통 예외
│   └── common-util/             # 공통 유틸리티
│
├── infrastructure/              # 인프라 서비스
│   ├── eureka-server/           # 서비스 디스커버리
│   ├── api-gateway/             # API 게이트웨이
│   └── config-server/           # 설정 서버
│
├── services/                    # 마이크로서비스
│   ├── user-service/
│   ├── notification-service/
│   ├── program-service/
│   ├── program-application-service/
│   ├── portfolio-service/
│   ├── consultation-service/
│   ├── competency-service/
│   ├── mileage-service/
│   ├── survey-service/
│   └── external-employment-service/
│
├── config-repo/                 # Config Server 설정 저장소
│   ├── user-service.yml
│   ├── notification-service.yml
│   └── ...
│
├── docker-compose.yml           # Docker 컨테이너 오케스트레이션
├── kubernetes/                  # Kubernetes 매니페스트 (선택)
│   ├── deployments/
│   ├── services/
│   └── ingress/
│
├── doc/                         # 문서
│   ├── msa-migration/           # MSA 마이그레이션 로그
│   ├── api-specs/               # API 명세
│   └── architecture/            # 아키텍처 문서
│
└── legacy/                      # 기존 모노리틱 코드 (참고용)
```

---

## 7. 주요 도전 과제 및 해결 방안

### 7.1 데이터 일관성
**문제:** 분산 트랜잭션 처리 어려움
**해결:**
- Saga 패턴 도입 (Orchestration 또는 Choreography)
- 보상 트랜잭션 (Compensating Transaction)
- 최종 일관성 허용 (이벤트 기반)

### 7.2 서비스 간 통신 장애
**문제:** 한 서비스 장애 시 전체 시스템 영향
**해결:**
- Circuit Breaker (Resilience4j)
- Fallback 메커니즘
- 타임아웃 및 재시도 정책

### 7.3 테스트 복잡도 증가
**문제:** 여러 서비스 동시 테스트 어려움
**해결:**
- Contract Testing (Spring Cloud Contract)
- 통합 테스트 환경 (Docker Compose)
- 모의 객체 (WireMock, Testcontainers)

### 7.4 모니터링 및 디버깅
**문제:** 분산 시스템 디버깅 어려움
**해결:**
- 분산 추적 (Zipkin, Jaeger)
- 중앙 집중식 로깅 (ELK)
- 상관 ID (Correlation ID) 전파

---

## 8. 성공 기준

### 8.1 기능적 요구사항
- [ ] 기존 모든 기능이 MSA 환경에서 정상 동작
- [ ] API 응답 시간 기존 대비 120% 이내 유지
- [ ] 데이터 일관성 보장 (최종 일관성 포함)

### 8.2 비기능적 요구사항
- [ ] 각 서비스 독립적으로 배포 가능
- [ ] 서비스 장애 시 다른 서비스에 영향 최소화 (Circuit Breaker 동작)
- [ ] 수평 확장 가능 (서비스별 스케일 아웃)
- [ ] 모니터링 대시보드를 통한 실시간 상태 확인

### 8.3 개발 효율성
- [ ] API 문서 자동 생성 (Swagger)
- [ ] 로컬 개발 환경 간편 구성 (Docker Compose)
- [ ] 서비스별 독립적 개발 가능

---

## 9. 위험 관리

| 위험 | 영향도 | 발생 가능성 | 대응 방안 |
|------|--------|-----------|----------|
| 데이터 마이그레이션 실패 | 높음 | 중간 | - Flyway 스크립트 사전 테스트<br>- 롤백 계획 수립 |
| 성능 저하 | 중간 | 높음 | - 성능 테스트 반복 실행<br>- 캐싱 전략 도입 (Redis) |
| 서비스 간 통신 장애 | 높음 | 중간 | - Circuit Breaker 적용<br>- Fallback 메커니즘 |
| 일정 지연 | 중간 | 높음 | - Phase별 우선순위 조정<br>- 점진적 마이그레이션 |
| 학습 곡선 | 낮음 | 높음 | - 기술 스터디 세션<br>- 레퍼런스 문서 작성 |

---

## 10. 다음 단계

1. ✅ **Phase 0 완료:** 프로젝트 분석 및 계획 수립
2. ⏭️ **Phase 1 시작:** 공통 라이브러리 모듈 생성
3. ⏭️ Eureka Server 구성
4. ⏭️ API Gateway 구성
5. ⏭️ Config Server 구성

---

## 11. 참고 자료

### 11.1 내부 문서
- `/database/schema.sql` - 데이터베이스 스키마
- `/src/main/resources/db/migration/` - Flyway 마이그레이션
- `/doc/` - 기존 프로젝트 문서

### 11.2 외부 참고
- Spring Cloud Documentation
- Microservices Patterns (Chris Richardson)
- Domain-Driven Design (Eric Evans)

---

**계획 수립 완료일:** 2025-11-19
**다음 로그:** `01-phase1-infrastructure.md`
