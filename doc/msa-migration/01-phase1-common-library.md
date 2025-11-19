# Phase 1-1: 공통 라이브러리 모듈 생성

**작성일:** 2025-11-19
**단계:** Phase 1 - 인프라 구축
**상태:** ✅ 완료

---

## 1. 작업 개요

### 1.1 목적
모든 마이크로서비스에서 공통으로 사용할 라이브러리 모듈을 생성하여 코드 재사용성을 높이고 일관된 개발 표준을 제공합니다.

### 1.2 범위
- 프로젝트 구조를 모노리틱에서 멀티 모듈로 전환
- 공통 DTO, 예외 처리, 유틸리티 클래스 생성
- Gradle 멀티 모듈 설정

---

## 2. 실행 작업

### 2.1 프로젝트 구조 변경

#### 기존 코드 백업
```bash
mkdir -p legacy-monolith
cp -r src legacy-monolith/
```

기존 모노리틱 코드를 `legacy-monolith/` 디렉토리에 백업하여 참조용으로 보관했습니다.

#### 멀티 모듈 디렉토리 생성
```bash
mkdir -p common-library/{common-dto,common-exception,common-util}
mkdir -p infrastructure/{eureka-server,api-gateway,config-server}
mkdir -p services
mkdir -p config-repo
```

**생성된 구조:**
```
SCMS4-MSA/
├── common-library/
│   ├── common-dto/          # 공통 DTO
│   ├── common-exception/    # 공통 예외
│   └── common-util/         # 공통 유틸리티
├── infrastructure/          # 인프라 서비스
│   ├── eureka-server/
│   ├── api-gateway/
│   └── config-server/
├── services/                # 비즈니스 마이크로서비스
├── config-repo/             # Config Server 설정 저장소
├── legacy-monolith/         # 기존 모노리틱 코드 (백업)
└── doc/msa-migration/       # MSA 마이그레이션 로그
```

### 2.2 Gradle 설정

#### settings.gradle
```gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = 'scms4-msa'

// Common Library Modules
include 'common-library:common-dto'
include 'common-library:common-exception'
include 'common-library:common-util'

// Infrastructure Modules
include 'infrastructure:eureka-server'
include 'infrastructure:api-gateway'
include 'infrastructure:config-server'
```

#### 루트 build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.0' apply false
    id 'io.spring.dependency-management' version '1.1.5' apply false
}

ext {
    springBootVersion = '3.3.0'
    springCloudVersion = '2023.0.2'
    lombokVersion = '1.18.30'
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    sourceCompatibility = '17'
    targetCompatibility = '17'

    dependencyManagement {
        imports {
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
        }
    }
}

// Common Library는 부트 가능한 JAR이 아님
configure(subprojects.findAll { it.path.startsWith(':common-library') }) {
    bootJar {
        enabled = false
    }
    jar {
        enabled = true
    }
}
```

**주요 변경 사항:**
- Spring Boot 3.3.0 유지
- **Spring Cloud 2023.0.2** 추가 (MSA 지원)
- 멀티 모듈 프로젝트로 전환

---

## 3. 공통 라이브러리 구현

### 3.1 common-dto 모듈

**패키지:** `com.scms.common.dto`

#### 생성된 클래스

| 클래스명 | 목적 | 주요 메서드 |
|---------|------|-----------|
| **ApiResponse<T>** | 공통 API 응답 포맷 | `success()`, `error()` |
| **ErrorResponse** | 에러 응답 DTO | 에러 코드, 메시지, 필드 에러 |
| **PageResponse<T>** | 페이지네이션 응답 | `of(Page<T>)` |

#### ApiResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> success(String message, T data) { ... }
    public static <T> ApiResponse<T> error(String message) { ... }
}
```

**사용 예시:**
```java
// 성공 응답
return ApiResponse.success("사용자 등록 완료", userResponse);

// 에러 응답
return ApiResponse.error("사용자를 찾을 수 없습니다.");
```

#### ErrorResponse.java
- 에러 코드, 메시지, 상세 정보
- 필드별 에러 (유효성 검증 실패 시)
- 타임스탬프, 요청 경로

#### PageResponse.java
- Spring Data의 `Page<T>`를 공통 형식으로 변환
- 페이지 번호, 크기, 전체 개수, 마지막 페이지 여부 등

---

### 3.2 common-exception 모듈

**패키지:** `com.scms.common.exception`

#### 생성된 클래스

| 클래스명 | 목적 | HTTP 상태 코드 |
|---------|------|---------------|
| **BaseException** | 모든 커스텀 예외의 부모 | - |
| **ResourceNotFoundException** | 리소스 미발견 | 404 NOT_FOUND |
| **DuplicateResourceException** | 중복 리소스 | 409 CONFLICT |
| **InvalidRequestException** | 잘못된 요청 | 400 BAD_REQUEST |
| **UnauthorizedException** | 인증 실패 | 401 UNAUTHORIZED |
| **ForbiddenException** | 권한 없음 | 403 FORBIDDEN |
| **GlobalExceptionHandler** | 전역 예외 핸들러 | - |

#### BaseException.java
```java
@Getter
public class BaseException extends RuntimeException {
    private final String errorCode;

    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

#### GlobalExceptionHandler.java
- `@RestControllerAdvice`를 사용한 전역 예외 처리
- 각 예외 타입별로 적절한 HTTP 상태 코드 반환
- `ErrorResponse` DTO로 일관된 에러 응답 제공
- 유효성 검증 실패 시 필드별 에러 정보 포함

**처리하는 예외:**
1. `ResourceNotFoundException` → 404
2. `DuplicateResourceException` → 409
3. `InvalidRequestException` → 400
4. `UnauthorizedException` → 401
5. `ForbiddenException` → 403
6. `MethodArgumentNotValidException` → 400 (유효성 검증 실패)
7. `Exception` → 500 (기타 모든 예외)

---

### 3.3 common-util 모듈

**패키지:** `com.scms.common.util`

#### 생성된 클래스

| 클래스명 | 목적 | 주요 메서드 |
|---------|------|-----------|
| **DateTimeUtils** | 날짜/시간 유틸리티 | `format()`, `parse()`, `daysBetween()` |
| **StringUtils** | 문자열 유틸리티 | `isEmpty()`, `mask()`, `maskEmail()` |

#### DateTimeUtils.java
**주요 기능:**
- 날짜/시간 포맷 변환 (문자열 ↔ LocalDate/LocalDateTime)
- 두 날짜 사이의 일수 계산
- 날짜/시간 범위 체크

**사용 예시:**
```java
String formatted = DateTimeUtils.format(LocalDateTime.now());
// "2025-11-19 10:30:00"

long days = DateTimeUtils.daysBetween(startDate, endDate);

boolean inRange = DateTimeUtils.isWithinRange(date, startDate, endDate);
```

#### StringUtils.java
**주요 기능:**
- null/빈 문자열 체크
- 문자열 마스킹 (개인정보 보호)
- 이메일/전화번호 마스킹
- 문자열 자르기

**사용 예시:**
```java
String masked = StringUtils.maskEmail("user@example.com");
// "u**@example.com"

String maskedPhone = StringUtils.maskPhone("010-1234-5678");
// "010-****-5678"
```

---

## 4. 의존성 설정

### common-dto/build.gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
}
```

### common-exception/build.gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation project(':common-library:common-dto')
}
```

### common-util/build.gradle
```gradle
dependencies {
    implementation 'org.springframework:spring-context'
    implementation 'org.apache.commons:commons-lang3:3.13.0'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```

---

## 5. 파일 목록

### 생성된 파일
```
common-library/
├── common-dto/
│   ├── build.gradle
│   └── src/main/java/com/scms/common/dto/
│       ├── ApiResponse.java
│       ├── ErrorResponse.java
│       └── PageResponse.java
│
├── common-exception/
│   ├── build.gradle
│   └── src/main/java/com/scms/common/exception/
│       ├── BaseException.java
│       ├── ResourceNotFoundException.java
│       ├── DuplicateResourceException.java
│       ├── InvalidRequestException.java
│       ├── UnauthorizedException.java
│       ├── ForbiddenException.java
│       └── GlobalExceptionHandler.java
│
└── common-util/
    ├── build.gradle
    └── src/main/java/com/scms/common/util/
        ├── DateTimeUtils.java
        └── StringUtils.java
```

**총 파일 수:** 13개 (Java 클래스 10개 + build.gradle 3개)

---

## 6. 테스트 계획

### 6.1 빌드 테스트
```bash
./gradlew :common-library:common-dto:build
./gradlew :common-library:common-exception:build
./gradlew :common-library:common-util:build
```

### 6.2 다음 단계에서 테스트
각 마이크로서비스 구현 시 공통 라이브러리를 의존성으로 추가하여 실제 동작 검증 예정

---

## 7. 주요 결정 사항

### 7.1 공통 응답 포맷 통일
- 모든 API 응답은 `ApiResponse<T>` 형식 사용
- 성공/실패 여부, 메시지, 데이터, 타임스탬프 포함
- 클라이언트가 일관된 형식으로 응답 처리 가능

### 7.2 예외 처리 표준화
- 모든 커스텀 예외는 `BaseException` 상속
- `GlobalExceptionHandler`로 중앙 집중식 예외 처리
- 에러 코드와 HTTP 상태 코드 매핑

### 7.3 유틸리티 클래스 분리
- 정적 메서드로 구성
- 특정 서비스에 의존하지 않음
- 순수 Java 로직만 포함

---

## 8. 다음 단계

### 8.1 즉시 수행
- ✅ 공통 라이브러리 모듈 생성 완료
- ⏭️ Eureka Server 구성
- ⏭️ API Gateway 구성
- ⏭️ Config Server 구성

### 8.2 향후 계획
- User Service 구현 시 공통 라이브러리 사용 테스트
- 필요에 따라 추가 유틸리티 클래스 보충
- 공통 보안 설정 클래스 추가 (JWT 토큰 검증 등)

---

## 9. 알려진 이슈 및 제약사항

### 9.1 현재 제약사항
- 공통 라이브러리는 단독 실행 불가 (부트 가능한 JAR 아님)
- 다른 모듈에 의존성으로 추가되어야 사용 가능

### 9.2 향후 개선 사항
- 공통 보안 설정 모듈 추가 (common-security)
- 공통 이벤트 DTO 모듈 추가 (common-event)
- Resilience4j 공통 설정 (common-resilience)

---

## 10. 참고 자료

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Gradle Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Spring Boot 3.3.0 Release Notes](https://spring.io/blog/2024/05/23/spring-boot-3-3-0-available-now)

---

**작업 완료:** 2025-11-19
**다음 로그:** `02-phase1-eureka-server.md`
**작업자:** Claude
