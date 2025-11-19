# SCMS4-MSA: 마이크로서비스 아키텍처 변환 프로젝트

**SCMS (Student Competency Management System)** 학생 역량 관리 시스템을 모노리틱 아키텍처에서 마이크로서비스 아키텍처(MSA)로 변환한 프로젝트입니다.

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [아키텍처](#-아키텍처)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [시작하기](#-시작하기)
- [마이크로서비스 목록](#-마이크로서비스-목록)
- [개발 로그](#-개발-로그)
- [향후 계획](#-향후-계획)

---

## 🎯 프로젝트 개요

### 변환 목적

기존 모노리틱 구조의 SCMS 시스템을 마이크로서비스 아키텍처로 전환하여:

- ✅ **확장성 향상**: 서비스별 독립적인 스케일링
- ✅ **장애 격리**: 특정 서비스 장애가 전체 시스템에 영향을 주지 않음
- ✅ **독립 배포**: 서비스별 독립적인 개발 및 배포
- ✅ **기술 스택 다양화**: 서비스별 최적화된 기술 선택 가능
- ✅ **팀 생산성**: 도메인별 팀 분리를 통한 병렬 개발

### 변환 전략

- **단계적 마이그레이션**: 일부 서비스부터 점진적으로 분리
- **Strangler Fig 패턴**: 기존 시스템을 유지하면서 신규 서비스로 대체
- **Database Per Service**: 각 마이크로서비스가 독립적인 데이터베이스 소유
- **Event-Driven Architecture**: RabbitMQ를 통한 비동기 이벤트 통신

---

## 🏗 아키텍처

### 전체 시스템 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│                   (Web Browser / Mobile App)                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                     API Gateway (8080)                           │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │ • 라우팅 • 인증 • Rate Limiting • Circuit Breaker        │  │
│   └──────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                  Eureka Server (8761)                            │
│                  Service Discovery & Registry                    │
└──────────────────┬────────────────┬───────────────┬─────────────┘
                   │                │               │
        ┌──────────▼───┐  ┌────────▼──────┐  ┌────▼────────┐
        │ User Service │  │   Program     │  │ Portfolio   │
        │    (8081)    │  │   Service     │  │  Service    │
        │   [MySQL]    │  │    (8083)     │  │   (8085)    │
        └──────┬───────┘  │   [MySQL]     │  │  [MySQL]    │
               │          └───────┬───────┘  └─────┬───────┘
               │                  │                │
┌──────────────▼──────────────────▼────────────────▼─────────────┐
│          RabbitMQ (5672) - Message Broker                       │
│          Event-Driven Communication                             │
└──────────────┬──────────────────┬────────────────┬─────────────┘
               │                  │                │
    ┌──────────▼───────┐  ┌──────▼──────┐  ┌─────▼──────┐
    │  Notification    │  │   Mileage   │  │  Survey    │
    │    Service       │  │   Service   │  │  Service   │
    │     (8082)       │  │   (8088)    │  │  (8089)    │
    │    [MySQL]       │  │   [MySQL]   │  │  [MySQL]   │
    └──────────────────┘  └─────────────┘  └────────────┘

┌──────────────────────────────────────────────────────────────────┐
│              Config Server (8888)                                │
│              중앙 집중식 설정 관리                                 │
└──────────────────────────────────────────────────────────────────┘
```

### 주요 패턴

#### 1. Service Discovery Pattern
- **Eureka Server**를 통한 동적 서비스 디스커버리
- 각 서비스는 시작 시 Eureka에 자동 등록
- 로드 밸런싱 자동 처리

#### 2. API Gateway Pattern
- 모든 클라이언트 요청의 단일 진입점
- 라우팅, 인증, CORS, Rate Limiting 중앙화
- Circuit Breaker를 통한 장애 격리

#### 3. Database Per Service
- 각 마이크로서비스는 독립적인 데이터베이스 보유
- 서비스 간 직접 DB 접근 금지
- API 또는 이벤트를 통해서만 데이터 공유

#### 4. Event-Driven Architecture
- RabbitMQ를 통한 비동기 메시징
- 느슨한 결합 (Loose Coupling)
- 최종 일관성 (Eventual Consistency)

---

## 🛠 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.3.0**
- **Spring Cloud 2023.0.2**
  - Netflix Eureka (Service Discovery)
  - Spring Cloud Gateway (API Gateway)
  - Spring Cloud Config (Configuration Management)
- **Spring Data JPA** / **Hibernate**
- **MySQL 8.0** (Database Per Service)
- **RabbitMQ 3.12** (Message Broker)
- **Flyway** (Database Migration)

### Security
- **Spring Security**
- **JWT (JSON Web Token)** - 인증/인가
- **OAuth2** - 소셜 로그인 (Google, Kakao, Naver)

### Resilience
- **Resilience4j** - Circuit Breaker, Retry, Rate Limiter
- **Spring Cloud LoadBalancer**

### DevOps
- **Docker** / **Docker Compose**
- **Gradle** (Multi-Module Project)

### Monitoring (향후 추가 예정)
- **Spring Cloud Sleuth** + **Zipkin** - 분산 추적
- **Prometheus** + **Grafana** - 메트릭 수집 및 시각화
- **ELK Stack** - 중앙 집중식 로깅

---

## 📁 프로젝트 구조

```
SCMS4-MSA/
├── common-library/               # 공통 라이브러리
│   ├── common-dto/               # 공통 DTO (ApiResponse, ErrorResponse, PageResponse)
│   ├── common-exception/         # 공통 예외 (BaseException, GlobalExceptionHandler)
│   └── common-util/              # 공통 유틸리티 (DateTimeUtils, StringUtils)
│
├── infrastructure/               # 인프라 서비스
│   ├── eureka-server/            # 서비스 디스커버리 (8761)
│   ├── api-gateway/              # API 게이트웨이 (8080)
│   └── config-server/            # 설정 서버 (8888)
│
├── services/                     # 비즈니스 마이크로서비스
│   ├── user-service/             # 사용자 관리 (8081)
│   ├── notification-service/     # 알림 관리 (8082)
│   ├── program-service/          # 프로그램 관리 (8083)
│   ├── program-application-service/  # 프로그램 신청 (8084)
│   ├── portfolio-service/        # 포트폴리오 (8085)
│   ├── consultation-service/     # 상담 관리 (8086)
│   ├── competency-service/       # 역량 관리 (8087)
│   ├── mileage-service/          # 마일리지 (8088)
│   ├── survey-service/           # 설문조사 (8089)
│   └── external-employment-service/  # 외부 고용 정보 (8090)
│
├── config-repo/                  # Config Server 설정 저장소
│   ├── application.yml           # 공통 설정
│   ├── user-service.yml          # User Service 설정
│   ├── notification-service.yml  # Notification Service 설정
│   └── ...
│
├── doc/                          # 문서
│   ├── msa-migration/            # MSA 마이그레이션 로그
│   │   ├── 00-migration-plan.md
│   │   ├── 01-phase1-common-library.md
│   │   ├── 02-phase1-infrastructure.md
│   │   └── ...
│   ├── api-specs/                # API 명세서 (Swagger/OpenAPI)
│   └── architecture/             # 아키텍처 문서
│
├── legacy-monolith/              # 기존 모노리틱 코드 (참고용)
│   └── src/
│
├── docker-compose.msa.yml        # MSA 전체 스택 Docker Compose
├── build.gradle                  # 루트 빌드 파일
├── settings.gradle               # 멀티 모듈 설정
└── README-MSA.md                 # 이 파일
```

---

## 🚀 시작하기

### 사전 요구사항

- **Java 17** 이상
- **Docker** & **Docker Compose**
- **Gradle 8.x**
- **MySQL 8.0** (로컬 개발 시)

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-org/SCMS4-MSA.git
cd SCMS4-MSA
```

### 2. 전체 빌드

```bash
./gradlew clean build
```

### 3. Docker Compose로 전체 스택 실행

```bash
# MSA 전체 스택 시작
docker-compose -f docker-compose.msa.yml up -d

# 로그 확인
docker-compose -f docker-compose.msa.yml logs -f

# 특정 서비스 로그만 확인
docker-compose -f docker-compose.msa.yml logs -f user-service

# 중지
docker-compose -f docker-compose.msa.yml down

# 전체 삭제 (볼륨 포함)
docker-compose -f docker-compose.msa.yml down -v
```

### 4. 개별 서비스 로컬 실행 (개발 모드)

**순서대로 실행 (의존성 있음):**

1. **Eureka Server** (필수)
   ```bash
   ./gradlew :infrastructure:eureka-server:bootRun
   ```
   접속: http://localhost:8761

2. **Config Server** (필수)
   ```bash
   ./gradlew :infrastructure:config-server:bootRun
   ```
   접속: http://localhost:8888

3. **API Gateway** (필수)
   ```bash
   ./gradlew :infrastructure:api-gateway:bootRun
   ```
   접속: http://localhost:8080

4. **User Service**
   ```bash
   ./gradlew :services:user-service:bootRun
   ```
   접속: http://localhost:8081

5. **Notification Service**
   ```bash
   ./gradlew :services:notification-service:bootRun
   ```
   접속: http://localhost:8082

### 5. 서비스 URL

| 서비스 | URL | 설명 |
|--------|-----|------|
| **Eureka Dashboard** | http://localhost:8761 | 서비스 레지스트리 확인 |
| **API Gateway** | http://localhost:8080 | 클라이언트 요청 진입점 |
| **Config Server** | http://localhost:8888 | 설정 확인 |
| **RabbitMQ Management** | http://localhost:15672 | 메시지 큐 관리 (admin/admin123) |
| User Service | http://localhost:8081 | 사용자 관리 API |
| Notification Service | http://localhost:8082 | 알림 관리 API |

---

## 📦 마이크로서비스 목록

### Infrastructure Services

| 서비스 | 포트 | 역할 | 상태 |
|--------|------|------|------|
| **Eureka Server** | 8761 | 서비스 디스커버리 | ✅ 완료 |
| **API Gateway** | 8080 | API 라우팅, 인증, Circuit Breaker | ✅ 완료 |
| **Config Server** | 8888 | 중앙 설정 관리 | ✅ 완료 |

### Business Services

| 서비스 | 포트 | 주요 기능 | 데이터베이스 | 상태 |
|--------|------|----------|-----------|------|
| **User Service** | 8081 | 사용자 CRUD, 로그인, JWT, OAuth2 | scms_user | ✅ 구조 완료 |
| **Notification Service** | 8082 | 시스템 알림, 이메일 발송 | scms_notification | ✅ 구조 완료 |
| **Program Service** | 8083 | 비교과 프로그램 관리 | scms_program | 🔄 계획됨 |
| **Program Application Service** | 8084 | 프로그램 신청/심사 | scms_application | 🔄 계획됨 |
| **Portfolio Service** | 8085 | 포트폴리오 관리 | scms_portfolio | 🔄 계획됨 |
| **Consultation Service** | 8086 | 상담 예약/기록 | scms_consultation | 🔄 계획됨 |
| **Competency Service** | 8087 | 역량 관리/평가 | scms_competency | 🔄 계획됨 |
| **Mileage Service** | 8088 | 마일리지 적립/사용 | scms_mileage | 🔄 계획됨 |
| **Survey Service** | 8089 | 설문조사 관리 | scms_survey | 🔄 계획됨 |
| **External Employment Service** | 8090 | 외부 고용 정보 | scms_employment | 🔄 계획됨 |

---

## 📚 개발 로그

MSA 마이그레이션 과정은 `doc/msa-migration/` 디렉토리에 상세히 기록되어 있습니다.

- **[00-migration-plan.md](doc/msa-migration/00-migration-plan.md)** - MSA 변환 전체 계획
- **[01-phase1-common-library.md](doc/msa-migration/01-phase1-common-library.md)** - 공통 라이브러리 모듈 구현
- **[02-phase1-infrastructure.md](doc/msa-migration/02-phase1-infrastructure.md)** - 인프라 서비스 구축 (Eureka, API Gateway, Config Server)

### Phase 요약

| Phase | 내용 | 상태 |
|-------|------|------|
| **Phase 0** | 프로젝트 분석 및 계획 수립 | ✅ 완료 |
| **Phase 1** | 인프라 구축 (공통 라이브러리, Eureka, Gateway, Config) | ✅ 완료 |
| **Phase 2** | 핵심 서비스 분리 (User, Notification) | ✅ 구조 완료 |
| **Phase 3** | 비즈니스 서비스 분리 (Program, Portfolio, Consultation, Competency) | 🔄 진행 중 |
| **Phase 4** | 나머지 서비스 분리 (Mileage, Survey, External Employment) | 🔄 계획됨 |
| **Phase 5** | 통합 테스트 및 최적화 | 🔄 계획됨 |

---

## 🔧 개발 가이드

### API 호출 예시

#### 1. User Service를 통한 로그인 (직접 호출)
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "studentNum": 2024001,
    "password": "password123"
  }'
```

#### 2. API Gateway를 통한 호출 (권장)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "studentNum": 2024001,
    "password": "password123"
  }'
```

#### 3. JWT 토큰을 사용한 인증 요청
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 새로운 마이크로서비스 추가하기

1. **서비스 모듈 생성**
   ```bash
   mkdir -p services/your-service/src/main/java/com/scms/yourservice
   ```

2. **build.gradle 작성**
   - 공통 라이브러리 의존성 추가
   - Spring Cloud 설정

3. **Application 클래스 생성**
   ```java
   @EnableDiscoveryClient
   @SpringBootApplication
   public class YourServiceApplication { ... }
   ```

4. **bootstrap.yml 작성**
   ```yaml
   spring:
     application:
       name: your-service
     cloud:
       config:
         uri: http://localhost:8888
   ```

5. **Config Server에 설정 추가**
   - `config-repo/your-service.yml` 생성

6. **API Gateway 라우팅 추가**
   - `GatewayConfig.java`에 라우팅 규칙 추가

7. **settings.gradle에 모듈 추가**
   ```gradle
   include 'services:your-service'
   ```

---

## 🧪 테스트

### 단위 테스트
```bash
./gradlew test
```

### 통합 테스트
```bash
./gradlew integrationTest
```

### 특정 서비스 테스트
```bash
./gradlew :services:user-service:test
```

---

## 📊 모니터링 및 관리

### Eureka Dashboard
- URL: http://localhost:8761
- 등록된 서비스 목록, 상태, 인스턴스 수 확인

### API Gateway Actuator
```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# 라우팅 정보
curl http://localhost:8080/actuator/gateway/routes

# Circuit Breaker 상태
curl http://localhost:8080/actuator/circuitbreakers
```

### Config Server 설정 조회
```bash
# User Service 설정 조회
curl http://localhost:8888/user-service/default

# 공통 설정 조회
curl http://localhost:8888/application/default
```

### RabbitMQ Management Console
- URL: http://localhost:15672
- Username: `admin`
- Password: `admin123`

---

## 🔒 보안

### JWT 토큰 기반 인증
- User Service에서 JWT 토큰 발급
- API Gateway에서 토큰 검증 (향후 구현)

### OAuth2 소셜 로그인
- Google, Kakao, Naver 로그인 지원
- `config-repo/user-service.yml`에서 설정

### CORS 설정
- API Gateway의 `CorsConfig.java`에서 관리
- 허용된 출처, 메서드, 헤더 설정

---

## 🐛 문제 해결

### 서비스가 Eureka에 등록되지 않을 때
1. Eureka Server가 먼저 실행되었는지 확인
2. `bootstrap.yml`에서 `eureka.client.serviceUrl.defaultZone` 확인
3. 네트워크 방화벽 설정 확인

### Config Server에서 설정을 가져오지 못할 때
1. Config Server가 실행 중인지 확인
2. `config-repo/` 디렉토리에 설정 파일이 있는지 확인
3. `spring.cloud.config.uri` 설정 확인

### Circuit Breaker가 OPEN 상태일 때
1. 대상 서비스가 정상 동작하는지 확인
2. `/actuator/circuitbreakers`로 상태 확인
3. 일정 시간 후 자동으로 HALF_OPEN으로 전환됨

---

## 🌟 향후 계획

### Short-term (1-2개월)
- [ ] 모든 비즈니스 서비스 구현 완료
- [ ] JWT 인증 통합 (API Gateway)
- [ ] 통합 테스트 작성
- [ ] API 문서 자동화 (Swagger/OpenAPI)

### Mid-term (3-6개월)
- [ ] 분산 추적 (Sleuth + Zipkin) 도입
- [ ] 중앙 로깅 (ELK Stack) 구축
- [ ] 모니터링 대시보드 (Prometheus + Grafana)
- [ ] Kubernetes 배포 manifest 작성

### Long-term (6-12개월)
- [ ] Service Mesh (Istio) 도입
- [ ] Event Sourcing & CQRS 패턴 적용
- [ ] GraphQL Gateway 도입 검토
- [ ] Multi-region 배포

---

## 📝 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다.

---

## 👥 기여자

- **Claude** - MSA 아키텍처 설계 및 구현

---

## 🔗 참고 자료

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Microservices Patterns (Chris Richardson)](https://microservices.io/patterns/)
- [Domain-Driven Design (Eric Evans)](https://www.domainlanguage.com/ddd/)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [Resilience4j](https://resilience4j.readme.io/)

---

**Last Updated:** 2025-11-19
