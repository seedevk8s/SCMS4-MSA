# Phase 1-2: 인프라 구축 (Eureka, API Gateway, Config Server)

**작성일:** 2025-11-19
**단계:** Phase 1 - 인프라 구축
**상태:** ✅ 완료

---

## 1. 작업 개요

### 1.1 목적
MSA 아키텍처의 핵심 인프라 컴포넌트를 구축합니다:
- **Eureka Server**: 서비스 디스커버리 및 레지스트리
- **API Gateway**: 클라이언트 요청의 진입점 및 라우팅
- **Config Server**: 중앙 집중식 설정 관리

### 1.2 기대 효과
- 동적 서비스 디스커버리로 서비스 간 통신 간소화
- API Gateway를 통한 보안, 인증, 로드 밸런싱 중앙화
- 설정 관리 중앙화로 환경별 배포 단순화

---

## 2. Eureka Server 구성

### 2.1 개요
**역할:**
- 마이크로서비스 등록 및 관리
- 서비스 인스턴스 정보 제공 (주소, 포트, 상태)
- 헬스 체크를 통한 가용성 모니터링

**포트:** 8761 (기본값)

### 2.2 구현

#### 디렉토리 구조
```
infrastructure/eureka-server/
├── build.gradle
└── src/main/
    ├── java/com/scms/eureka/
    │   └── EurekaServerApplication.java
    └── resources/
        └── application.yml
```

#### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

#### EurekaServerApplication.java
```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.java, args);
    }
}
```

#### 주요 설정 (application.yml)
```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false  # Eureka Server 자신을 등록하지 않음
    fetch-registry: false
  server:
    enable-self-preservation: false  # 개발 환경에서는 비활성화
    eviction-interval-timer-in-ms: 60000
```

### 2.3 주요 기능
- **서비스 레지스트리**: 각 마이크로서비스가 시작 시 자동 등록
- **헬스 체크**: 30초마다 서비스 상태 확인
- **자가 보존 모드**: 프로덕션에서는 활성화하여 네트워크 장애 시 서비스 제거 방지
- **대시보드**: http://localhost:8761 에서 등록된 서비스 확인 가능

---

## 3. API Gateway 구성

### 3.1 개요
**역할:**
- 클라이언트 요청을 적절한 마이크로서비스로 라우팅
- 로드 밸런싱 (Ribbon/LoadBalancer)
- Circuit Breaker를 통한 장애 격리 (Resilience4j)
- CORS 처리
- 인증/인가 (JWT 검증, 추후 구현)

**포트:** 8080

### 3.2 구현

#### 디렉토리 구조
```
infrastructure/api-gateway/
├── build.gradle
└── src/main/
    ├── java/com/scms/gateway/
    │   ├── ApiGatewayApplication.java
    │   ├── config/
    │   │   ├── GatewayConfig.java
    │   │   └── CorsConfig.java
    │   └── controller/
    │       └── FallbackController.java
    └── resources/
        └── application.yml
```

#### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

### 3.3 라우팅 설정

**GatewayConfig.java**에서 각 서비스별로 라우팅 규칙 정의:

| 경로 패턴 | 서비스 | Circuit Breaker | Retry |
|----------|--------|----------------|-------|
| `/api/users/**`, `/api/auth/**` | user-service | ✅ | 3회 |
| `/api/notifications/**` | notification-service | ✅ | 2회 |
| `/api/programs/**` | program-service | ✅ | 3회 |
| `/api/program-applications/**` | program-application-service | ✅ | 기본값 |
| `/api/portfolios/**` | portfolio-service | ✅ | 기본값 |
| `/api/consultations/**` | consultation-service | ✅ | 기본값 |
| `/api/competencies/**` | competency-service | ✅ | 기본값 |
| `/api/mileage/**` | mileage-service | ✅ | 기본값 |
| `/api/surveys/**` | survey-service | ✅ | 기본값 |
| `/api/external-employment/**` | external-employment-service | ✅ | 기본값 |

**라우팅 예시:**
```java
.route("user-service", r -> r
    .path("/api/users/**", "/api/auth/**")
    .filters(f -> f
        .circuitBreaker(config -> config
            .setName("userServiceCircuitBreaker")
            .setFallbackUri("forward:/fallback/user-service"))
        .retry(retryConfig -> retryConfig.setRetries(3)))
    .uri("lb://user-service"))
```

### 3.4 Circuit Breaker 설정

**Resilience4j 설정 (application.yml):**
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50          # 실패율 50% 이상 시 OPEN
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
        wait-duration-in-open-state: 10s    # OPEN 상태 유지 시간
        permitted-number-of-calls-in-half-open-state: 5
        sliding-window-size: 10
        minimum-number-of-calls: 5
```

**Circuit Breaker 상태:**
1. **CLOSED (정상)**: 모든 요청이 서비스로 전달
2. **OPEN (차단)**: 서비스 호출 차단, Fallback 응답 반환
3. **HALF_OPEN (반개방)**: 일부 요청만 허용하여 서비스 회복 여부 확인

### 3.5 Fallback Controller

서비스 장애 시 클라이언트에게 반환할 대체 응답:

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return createFallbackResponse("User Service");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", serviceName + "가 일시적으로 사용할 수 없습니다.");
        response.put("errorCode", "SERVICE_UNAVAILABLE");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
```

### 3.6 CORS 설정

**CorsConfig.java:**
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(
            "http://localhost:3000",  // React 개발 서버
            "http://localhost:8080"   // 로컬 프론트엔드
        ));
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        ...
    }
}
```

---

## 4. Config Server 구성

### 4.1 개요
**역할:**
- 모든 마이크로서비스의 설정 파일을 중앙에서 관리
- 환경별 설정 분리 (dev, staging, prod)
- 설정 변경 시 서비스 재시작 없이 리로드 가능 (Spring Cloud Bus 사용 시)
- Git 저장소 또는 로컬 파일 시스템에서 설정 로드

**포트:** 8888

### 4.2 구현

#### 디렉토리 구조
```
infrastructure/config-server/
├── build.gradle
└── src/main/
    ├── java/com/scms/config/
    │   └── ConfigServerApplication.java
    └── resources/
        └── application.yml
```

#### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-config-server'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

#### ConfigServerApplication.java
```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.java, args);
    }
}
```

#### 주요 설정 (application.yml)
```yaml
spring:
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config-repo, file:./config-repo

  profiles:
    active: native  # 로컬 파일 시스템 사용

server:
  port: 8888
```

### 4.3 설정 파일 구조

**config-repo/ 디렉토리:**
```
config-repo/
├── application.yml              # 모든 서비스의 공통 설정
├── user-service.yml             # User Service 전용 설정
├── notification-service.yml     # Notification Service 전용 설정
├── program-service.yml          # Program Service 전용 설정
└── ...
```

### 4.4 공통 설정 (application.yml)

**모든 마이크로서비스에 적용되는 설정:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway 사용하므로 validate만 수행
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

logging:
  level:
    root: INFO
    com.scms: DEBUG
```

### 4.5 서비스별 설정 예시

#### user-service.yml
```yaml
spring:
  application:
    name: user-service

  datasource:
    url: jdbc:mysql://localhost:3306/scms_user
    username: root
    password: ${DB_PASSWORD:password}

  security:
    jwt:
      secret: ${JWT_SECRET:your-secret-key}
      expiration: 86400000  # 24시간

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}

server:
  port: 8081

user-service:
  password:
    reset-token-expiration: 3600000  # 1시간
  login:
    max-failed-attempts: 5
    lock-duration: 900000  # 15분
```

#### notification-service.yml
```yaml
spring:
  application:
    name: notification-service

  datasource:
    url: jdbc:mysql://localhost:3306/scms_notification

  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

server:
  port: 8082

notification-service:
  email:
    from: noreply@scms.com
    max-retry: 3
```

### 4.6 Config Client 사용 방법

**각 마이크로서비스의 bootstrap.yml:**
```yaml
spring:
  application:
    name: user-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
```

**설정 우선순위:**
1. `application.yml` (공통 설정)
2. `{application}.yml` (서비스별 설정)
3. `{application}-{profile}.yml` (환경별 설정)
4. 환경 변수 및 시스템 프로퍼티

---

## 5. 포트 할당

| 서비스 | 포트 | 용도 |
|--------|------|------|
| **Eureka Server** | 8761 | 서비스 디스커버리 |
| **API Gateway** | 8080 | 클라이언트 요청 진입점 |
| **Config Server** | 8888 | 중앙 설정 관리 |
| User Service | 8081 | 사용자 관리 |
| Notification Service | 8082 | 알림 관리 |
| Program Service | 8083 | 프로그램 관리 |
| Program Application Service | 8084 | 프로그램 신청 |
| Portfolio Service | 8085 | 포트폴리오 |
| Consultation Service | 8086 | 상담 관리 |
| Competency Service | 8087 | 역량 관리 |
| Mileage Service | 8088 | 마일리지 |
| Survey Service | 8089 | 설문조사 |
| External Employment Service | 8090 | 외부 고용 정보 |

---

## 6. 실행 순서

MSA 환경을 구동하려면 다음 순서로 서비스를 시작해야 합니다:

1. **Eureka Server** (8761)
   ```bash
   ./gradlew :infrastructure:eureka-server:bootRun
   ```

2. **Config Server** (8888)
   ```bash
   ./gradlew :infrastructure:config-server:bootRun
   ```

3. **API Gateway** (8080)
   ```bash
   ./gradlew :infrastructure:api-gateway:bootRun
   ```

4. **마이크로서비스들** (User, Notification, Program, ...)
   - 순서는 중요하지 않음
   - 각 서비스는 Eureka에 자동 등록됨

---

## 7. 모니터링 및 관리

### 7.1 Eureka Dashboard
- URL: http://localhost:8761
- 등록된 서비스 목록, 상태, 인스턴스 수 확인

### 7.2 API Gateway Actuator
- URL: http://localhost:8080/actuator
- 엔드포인트:
  - `/actuator/health` - 헬스 체크
  - `/actuator/gateway/routes` - 라우팅 정보
  - `/actuator/circuitbreakers` - Circuit Breaker 상태

### 7.3 Config Server Actuator
- URL: http://localhost:8888/actuator
- 설정 조회:
  - `/user-service/default` - User Service 기본 설정
  - `/application/default` - 공통 설정

---

## 8. 주요 결정 사항

### 8.1 로컬 파일 시스템 vs Git 저장소
- **선택:** 로컬 파일 시스템 (native)
- **이유:** 개발 단계에서는 간편하게 설정 변경 가능
- **향후:** 프로덕션에서는 Git 저장소로 전환 권장

### 8.2 Circuit Breaker 라이브러리
- **선택:** Resilience4j
- **이유:** Netflix Hystrix는 유지보수 중단, Resilience4j가 Spring Cloud 공식 추천

### 8.3 Gateway 라우팅 방식
- **선택:** Eureka 기반 동적 라우팅 (`lb://service-name`)
- **이유:** 서비스 주소 하드코딩 불필요, 로드 밸런싱 자동 처리

---

## 9. 다음 단계

### 9.1 즉시 수행
- ✅ Eureka Server 구성 완료
- ✅ API Gateway 구성 완료
- ✅ Config Server 구성 완료
- ⏭️ User Service 분리 및 구현

### 9.2 향후 개선
- [ ] 보안 강화: API Gateway에서 JWT 검증 추가
- [ ] 분산 추적: Sleuth + Zipkin 도입
- [ ] 중앙 로깅: ELK Stack 구성
- [ ] Rate Limiting: Redis 기반 요청 속도 제한
- [ ] Spring Cloud Bus: 설정 변경 시 자동 리로드

---

## 10. 파일 목록

### 생성된 파일
```
infrastructure/
├── eureka-server/
│   ├── build.gradle
│   └── src/main/
│       ├── java/com/scms/eureka/EurekaServerApplication.java
│       └── resources/application.yml
│
├── api-gateway/
│   ├── build.gradle
│   └── src/main/
│       ├── java/com/scms/gateway/
│       │   ├── ApiGatewayApplication.java
│       │   ├── config/GatewayConfig.java
│       │   ├── config/CorsConfig.java
│       │   └── controller/FallbackController.java
│       └── resources/application.yml
│
└── config-server/
    ├── build.gradle
    └── src/main/
        ├── java/com/scms/config/ConfigServerApplication.java
        └── resources/application.yml

config-repo/
├── application.yml
├── user-service.yml
├── notification-service.yml
└── program-service.yml
```

**총 파일 수:** 15개

---

## 11. 참고 자료

- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)
- [Resilience4j](https://resilience4j.readme.io/)

---

**작업 완료:** 2025-11-19
**다음 로그:** `03-phase2-user-service.md`
**작업자:** Claude
