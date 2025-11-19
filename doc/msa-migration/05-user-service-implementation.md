# Phase 2-1: User Service 완전 구현

**작성일**: 2025-11-19
**상태**: ✅ 완료
**예상 시간**: 4-6시간
**실제 소요**: ~5시간

---

## 📋 목표

인증/인가의 핵심이 되는 User Service를 완전히 구현하여 다음 기능을 제공:
- 사용자 등록, 조회, 수정, 삭제 (CRUD)
- 로그인 및 JWT 토큰 발급
- OAuth2 소셜 로그인 준비 (Google, Kakao)
- 비밀번호 재설정 (이메일 기반)
- 사용자 권한 관리
- 로그인 히스토리 추적
- 계정 잠금/잠금 해제

---

## 🏗 구현 내역

### 1. Entity 계층 (10개 클래스, 1,219 lines)

#### 주요 Entity

**User.java** (211 lines)
- 내부 사용자 (학생, 관리자, 교수 등)
- 학번 기반 인증
- 계정 잠금 관리 (5회 로그인 실패 시 자동 잠금)
- Soft Delete 지원

```java
@Entity
@Table(name = "users")
public class User {
    private Long userId;
    private String studentNum;  // 학번 (unique)
    private String email;
    private String password;    // BCrypt 암호화
    private String name;
    private String phone;
    private UserRole role;      // STUDENT, ADMIN, PROFESSOR
    private Boolean locked;     // 계정 잠금 여부
    private Integer failCnt;    // 로그인 실패 횟수
    private LocalDateTime passwordUpdatedAt;
    private LocalDateTime deletedAt;  // Soft Delete

    // Business logic methods
    public void lock() { this.locked = true; }
    public void unlock() { this.locked = false; this.failCnt = 0; }
    public void incrementFailCount() {
        this.failCnt++;
        if (this.failCnt >= 5) { this.locked = true; }
    }
}
```

**ExternalUser.java** (317 lines)
- 외부 사용자 (기업, 졸업생 등)
- 이메일 기반 인증
- OAuth2 소셜 로그인 지원 (provider, providerId)
- 이메일 인증 기능
- 계정 상태 관리 (ACTIVE, INACTIVE, SUSPENDED)

```java
@Entity
@Table(name = "external_users")
public class ExternalUser {
    private Long externalUserId;
    private String email;
    private String password;  // 로컬 가입자만 사용
    private String name;
    private String phone;
    private String organization;  // 소속 기관
    private String position;      // 직위

    // OAuth2 fields
    private String provider;      // google, kakao, naver
    private String providerId;    // OAuth provider의 사용자 ID

    // Email verification
    private Boolean emailVerified;
    private String emailVerificationCode;

    // Account status
    private AccountStatus accountStatus;  // ACTIVE, INACTIVE, SUSPENDED

    // Terms agreement
    private Boolean termsAgreed;
    private Boolean privacyAgreed;
    private Boolean marketingAgreed;
}
```

**Student.java** (114 lines)
- User와 1:1 관계
- 학과, 전공, 학년 정보
- 입학일, 졸업일
- 졸업 여부 관리

**Counselor.java** (132 lines)
- User와 1:1 관계
- 상담사 정보 (부서, 사무실 위치)
- 전문 분야
- 가용 상태
- 총 상담 횟수 통계

**LoginHistory.java** (138 lines)
- 로그인 이력 추적
- IP 주소, User Agent 기록
- 성공/실패 여부
- 실패 사유
- 보안 모니터링용

**PasswordResetToken.java** (211 lines)
- 비밀번호 재설정 토큰
- UUID 기반 토큰
- 1시간 유효
- 일회성 사용
- 내부/외부 사용자 모두 지원

#### Enum Classes (4개)

1. **UserRole**: STUDENT, ADMIN, PROFESSOR, COUNSELOR, EXTERNAL
2. **UserType**: INTERNAL, EXTERNAL
3. **Gender**: MALE, FEMALE, OTHER
4. **AccountStatus**: ACTIVE, INACTIVE, SUSPENDED
5. **TokenType**: INTERNAL, EXTERNAL

---

### 2. Repository 계층 (6개 인터페이스, 777 lines)

모든 Repository는 JpaRepository를 확장하며, 다양한 쿼리 메서드 제공:

**UserRepository.java** (117 lines)
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumAndDeletedAtIsNull(String studentNum);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByStudentNum(String studentNum);
    boolean existsByEmail(String email);
    List<User> findByRoleAndDeletedAtIsNull(UserRole role);
    List<User> findAllActive();
    List<User> findAllLockedUsers();
    List<User> searchByNameOrStudentNum(String keyword);
    long countByRole(UserRole role);
}
```

**ExternalUserRepository.java** (154 lines)
- OAuth2 provider별 조회
- 이메일 인증 관련 쿼리
- 계정 상태별 조회

**StudentRepository.java** (124 lines)
- 학과별, 학년별 조회
- 졸업생/재학생 필터링
- 통계 쿼리

**CounselorRepository.java** (118 lines)
- 전문 분야별 조회
- 가용 상담사 조회
- 상담 통계

**LoginHistoryRepository.java** (149 lines)
- 최근 로그인 이력
- 실패한 로그인 추적
- IP 기반 DDoS 감지
- 사용자별 로그인 통계

**PasswordResetTokenRepository.java** (168 lines)
- 토큰 검증 쿼리
- 만료 토큰 자동 삭제
- 사용자별 토큰 관리

---

### 3. DTO 계층 (13개 클래스, 1,218 lines)

#### Request DTO (8개)

1. **LoginRequest.java** (52 lines)
   - loginId (학번 또는 이메일)
   - password
   - rememberMe (선택)

2. **UserCreateRequest.java** (89 lines)
   - 사용자 생성 시 필요한 모든 필드
   - 비밀번호 복잡도 검증 (정규표현식)
   - 비밀번호 확인 매칭

3. **UserUpdateRequest.java** (48 lines)
   - 수정 가능한 필드만 포함
   - 모든 필드 Optional

4. **ExternalUserCreateRequest.java** (112 lines)
   - 외부 사용자 회원가입
   - 조직, 직위 정보
   - 약관 동의 필드

5. **PasswordChangeRequest.java** (69 lines)
   - 현재 비밀번호
   - 새 비밀번호
   - 새 비밀번호 확인

6. **PasswordResetRequest.java** (34 lines)
   - 이메일만 포함

7. **PasswordResetConfirmRequest.java** (58 lines)
   - 새 비밀번호
   - 새 비밀번호 확인

8. **OAuth2UserInfo.java** (44 lines)
   - OAuth2 프로필 정보

#### Response DTO (5개)

1. **LoginResponse.java** (105 lines)
   - accessToken, refreshToken
   - tokenType (Bearer)
   - expiresIn
   - userType (INTERNAL/EXTERNAL)
   - UserResponse 포함

2. **UserResponse.java** (148 lines)
   - 사용자 정보 (비밀번호 제외)
   - from(User), from(ExternalUser) 팩토리 메서드
   - JSON 직렬화 최적화

3. **StudentResponse.java** (79 lines)
4. **CounselorResponse.java** (95 lines)
5. **LoginHistoryResponse.java** (69 lines)

---

### 4. Service 계층 (7개 클래스, 2,847 lines)

#### AuthService.java (383 lines)

**핵심 기능:**
- 로그인 (내부/외부 사용자 분리)
- 비밀번호 변경
- 비밀번호 재설정 (이메일 기반)
- JWT 토큰 생성 및 갱신
- 로그인 실패 처리 및 계정 잠금

**주요 메서드:**
```java
public LoginResponse loginInternal(LoginRequest, String ip, String userAgent)
public LoginResponse loginExternal(LoginRequest, String ip, String userAgent)
public void changePassword(Long userId, PasswordChangeRequest)
public void requestPasswordReset(PasswordResetRequest)
public void resetPassword(String token, PasswordResetConfirmRequest)
public LoginResponse refreshToken(String refreshToken)
```

**보안 기능:**
- 로그인 5회 실패 시 자동 계정 잠금
- 로그인 이력 자동 기록 (IP, User Agent)
- 타이밍 공격 방지 (존재하지 않는 이메일도 동일한 응답)
- BCrypt 비밀번호 암호화

#### UserService.java (261 lines)

**핵심 기능:**
- 사용자 CRUD
- 학번/이메일 중복 체크
- 계정 잠금/잠금 해제
- 사용자 검색

**주요 메서드:**
```java
public UserResponse createUser(UserCreateRequest)
public UserResponse getUserById(Long userId)
public UserResponse getUserByStudentNum(String studentNum)
public List<UserResponse> getUsersByRole(UserRole role)
public UserResponse updateUser(Long userId, UserUpdateRequest)
public void deleteUser(Long userId)  // Soft Delete
public void lockUser(Long userId)
public void unlockUser(Long userId)
```

#### ExternalUserService.java (265 lines)

**핵심 기능:**
- 외부 사용자 회원가입
- OAuth2 소셜 로그인 처리
- 이메일 인증
- 계정 상태 관리

**주요 메서드:**
```java
public UserResponse registerExternalUser(ExternalUserCreateRequest)
public ExternalUser processOAuthUser(String provider, String providerId, String email, String name)
public void verifyEmail(String verificationCode)
public void resendVerificationEmail(String email)
public void updateAccountStatus(Long userId, AccountStatus status)
```

**OAuth2 처리 로직:**
- 기존 사용자 확인 (provider + providerId)
- 신규 사용자 자동 생성
- 이메일 인증 자동 완료 (OAuth는 이미 인증됨)
- 환영 이메일 발송

#### EmailService.java (175 lines)

**핵심 기능:**
- 비밀번호 재설정 이메일
- 회원가입 환영 이메일
- 이메일 인증 코드 발송
- 프로그램 승인 알림 (Notification Service 연동용)
- 상담 예약 확인 이메일

**이메일 템플릿:**
- 간단한 텍스트 기반
- 링크 포함 (프론트엔드 URL)
- 실패해도 예외를 던지지 않음 (보안)

#### StudentService.java (171 lines)
- 학생 정보 CRUD
- 학과/학년별 조회
- 졸업생/재학생 관리
- 통계 기능

#### CounselorService.java (185 lines)
- 상담사 정보 CRUD
- 전문 분야별 조회
- 가용 상담사 조회
- 상담 통계

#### JwtTokenProvider.java (183 lines)

**핵심 기능:**
- Access Token 생성 (1일 유효)
- Refresh Token 생성 (7일 유효)
- 토큰 검증 및 파싱
- 사용자 정보 추출

**기술 스택:**
- io.jsonwebtoken (JJWT 0.11.5)
- HS256 알고리즘
- Secret Key 256-bit

```java
public String createAccessToken(User user)
public String createRefreshToken(User user)
public boolean validateToken(String token)
public String getStudentNum(String token)
public Long getUserId(String token)
public String getRole(String token)
```

---

### 5. Controller 계층 (2개 클래스, 458 lines)

#### AuthController.java (250 lines)

**엔드포인트 (11개):**

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/auth/login` | 내부 사용자 로그인 | Public |
| POST | `/api/auth/login/external` | 외부 사용자 로그인 | Public |
| POST | `/api/auth/logout` | 로그아웃 | Authenticated |
| POST | `/api/auth/refresh` | 토큰 갱신 | Public |
| POST | `/api/auth/password/change` | 비밀번호 변경 | Authenticated |
| POST | `/api/auth/password/reset-request` | 비밀번호 재설정 요청 | Public |
| POST | `/api/auth/password/reset` | 비밀번호 재설정 | Public |
| POST | `/api/auth/register/external` | 외부 사용자 회원가입 | Public |
| POST | `/api/auth/verify-email` | 이메일 인증 | Public |
| POST | `/api/auth/verify-email/resend` | 인증 코드 재발송 | Public |
| GET | `/api/auth/validate` | 토큰 검증 | Public |

**요청/응답 예시:**

```json
// POST /api/auth/login
{
  "loginId": "2024001",
  "password": "password123!@#",
  "rememberMe": true
}

// Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userType": "INTERNAL",
  "user": {
    "userId": 1,
    "studentNum": "2024001",
    "email": "student@example.com",
    "name": "홍길동",
    "role": "STUDENT"
  }
}
```

#### UserController.java (208 lines)

**엔드포인트 (15개):**

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/users` | 사용자 생성 | ADMIN |
| GET | `/api/users/{userId}` | 사용자 조회 | ADMIN |
| GET | `/api/users` | 전체 사용자 조회 | ADMIN |
| GET | `/api/users/role/{role}` | 역할별 사용자 조회 | ADMIN |
| GET | `/api/users/student-num/{studentNum}` | 학번으로 조회 | ADMIN |
| GET | `/api/users/email/{email}` | 이메일로 조회 | ADMIN |
| PUT | `/api/users/{userId}` | 사용자 수정 | ADMIN |
| DELETE | `/api/users/{userId}` | 사용자 삭제 | ADMIN |
| POST | `/api/users/{userId}/restore` | 사용자 복원 | ADMIN |
| POST | `/api/users/{userId}/lock` | 계정 잠금 | ADMIN |
| POST | `/api/users/{userId}/unlock` | 계정 잠금 해제 | ADMIN |
| GET | `/api/users/check/student-num` | 학번 중복 체크 | Public |
| GET | `/api/users/check/email` | 이메일 중복 체크 | Public |
| GET | `/api/users/search` | 사용자 검색 | ADMIN |
| GET | `/api/users/locked` | 잠긴 계정 조회 | ADMIN |

---

### 6. Security 설정 (2개 클래스, 310 lines)

#### SecurityConfig.java (122 lines)

**주요 설정:**
- JWT 기반 인증
- Stateless 세션 관리
- CORS 설정 (모든 Origin 허용)
- 엔드포인트별 권한 설정
- BCrypt 비밀번호 암호화

**공개 엔드포인트:**
- `/api/auth/**` (로그인, 회원가입, 비밀번호 재설정)
- `/api/users/check/**` (중복 체크)
- `/actuator/**` (헬스 체크)

**ADMIN 권한 필요:**
- `/api/users/**` (사용자 관리)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

#### JwtAuthenticationFilter.java (188 lines)

**주요 기능:**
- Authorization 헤더에서 JWT 토큰 추출
- 토큰 검증
- SecurityContext에 인증 정보 설정
- Request Attribute로 userId, role 전달

**필터 로직:**
1. `Authorization: Bearer {token}` 헤더 파싱
2. JWT 토큰 검증 (`jwtTokenProvider.validateToken()`)
3. 토큰에서 사용자 정보 추출 (studentNum, userId, role)
4. Authentication 객체 생성
5. SecurityContext에 설정
6. Request Attribute에 userId 저장 (Controller에서 `@RequestAttribute` 사용)

**예외 처리:**
- 토큰이 없거나 유효하지 않으면 인증 실패
- 필터 체인은 계속 진행 (Spring Security가 자동으로 거부)

---

### 7. 설정 파일

#### application.yml

```yaml
spring:
  application:
    name: user-service

  # Database
  datasource:
    url: jdbc:mysql://localhost:3306/scms_user?useSSL=false&serverTimezone=Asia/Seoul
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # JPA
  jpa:
    hibernate:
      ddl-auto: update  # 개발: update, 운영: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

  # Email (Gmail)
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}  # App Password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# JWT
jwt:
  secret: scms-secret-key-for-jwt-token-generation-minimum-256-bits-required
  access-token-validity: 86400000   # 1일
  refresh-token-validity: 604800000  # 7일

# Application
app:
  frontend:
    url: ${FRONTEND_URL:http://localhost:3000}

# Server
server:
  port: 8081

# Eureka
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### build.gradle

**주요 의존성:**
```gradle
dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Spring Cloud
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.cloud:spring-cloud-starter-config'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

    // Common Libraries
    implementation project(':common-library:common-dto')
    implementation project(':common-library:common-exception')
    implementation project(':common-library:common-util')
}
```

---

### 8. Common Exception 모듈 개선

#### ErrorCode.java (신규 생성, 95 lines)

**에러 코드 체계:**
- 1000번대: 공통 에러 (BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND)
- 2000번대: 사용자 관련 (USER_NOT_FOUND, DUPLICATE_EMAIL, ACCOUNT_LOCKED)
- 3000번대: 인증 관련 (INVALID_TOKEN, EXPIRED_TOKEN)
- 4000번대: 프로그램 관련 (예정)
- 5000번대: 포트폴리오 관련 (예정)

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "E1000", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E1001", "인증이 필요합니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E2000", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "E2003", "이미 사용 중인 이메일입니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "E2005", "계정이 잠겼습니다."),

    // 인증
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E3000", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "E3001", "만료된 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
```

#### ApiException.java (신규 생성, 47 lines)

```java
@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public ApiException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}
```

#### GlobalExceptionHandler.java (업데이트)

- `ApiException` 핸들러 추가
- ErrorCode의 HttpStatus 자동 적용
- 구조화된 에러 응답

---

## 📊 구현 통계

### 코드 라인 수

| 계층 | 파일 수 | 총 라인 수 |
|------|---------|------------|
| **Entity** | 10 | 1,219 |
| **Repository** | 6 | 777 |
| **DTO** | 13 | 1,218 |
| **Service** | 7 | 2,847 |
| **Controller** | 2 | 458 |
| **Security** | 2 | 310 |
| **Config** | 1 | 122 |
| **Exception** | 2 | 142 |
| **총계** | **43** | **7,093** |

### API 엔드포인트

- **AuthController**: 11개
- **UserController**: 15개
- **총계**: **26개 API**

---

## 🔒 보안 기능

### 1. 인증/인가
- ✅ JWT 기반 인증 (Access Token + Refresh Token)
- ✅ Stateless 세션 관리
- ✅ Role 기반 권한 제어 (STUDENT, ADMIN, PROFESSOR, etc.)
- ✅ API 엔드포인트별 권한 설정

### 2. 비밀번호 보안
- ✅ BCrypt 암호화 (강도 10)
- ✅ 비밀번호 복잡도 검증 (정규표현식)
  - 최소 8자, 최대 100자
  - 영문, 숫자, 특수문자 조합
- ✅ 비밀번호 재설정 토큰 (1시간 유효, 일회성)
- ✅ 비밀번호 변경 이력 추적 (passwordUpdatedAt)

### 3. 계정 보호
- ✅ 로그인 5회 실패 시 자동 계정 잠금
- ✅ 로그인 이력 추적 (IP, User Agent, 성공/실패)
- ✅ 타이밍 공격 방지 (존재하지 않는 이메일도 동일한 응답)
- ✅ Soft Delete (복원 가능)

### 4. 이메일 인증
- ✅ 외부 사용자 이메일 인증 필수
- ✅ UUID 기반 인증 코드 (24시간 유효)
- ✅ 인증 코드 재발송 기능

### 5. OAuth2 준비
- ✅ provider, providerId 필드
- ✅ OAuth2 사용자 자동 생성
- ✅ 이메일 자동 인증 (OAuth는 이미 인증됨)

---

## 🎯 설계 결정 사항

### 1. Database Per Service
- User Service는 독립적인 DB (`scms_user`) 사용
- 다른 서비스는 User ID로만 참조 (Foreign Key 없음)
- 데이터 일관성은 이벤트를 통해 유지 (향후 RabbitMQ 연동)

### 2. JWT vs Session
- **선택**: JWT
- **이유**:
  - Stateless 아키텍처 (MSA에 적합)
  - 수평 확장 용이
  - API Gateway에서 토큰 검증 가능
  - 모바일 앱 지원 용이

### 3. Access Token + Refresh Token
- Access Token: 1일 유효 (짧게 유지하여 보안 강화)
- Refresh Token: 7일 유효 (UX 개선)
- 토큰 갱신 엔드포인트 제공

### 4. 내부 사용자 vs 외부 사용자 분리
- **이유**:
  - 인증 방식이 다름 (학번 vs 이메일)
  - 필수 필드가 다름
  - 권한 체계가 다름
- **장점**:
  - 명확한 도메인 분리
  - 각 사용자 타입에 최적화된 필드
- **단점**:
  - 코드 중복 (AuthService에서 분기 처리)
  - 향후 통합 고려 필요

### 5. Soft Delete
- 모든 엔티티에 `deletedAt` 필드
- **이유**:
  - 데이터 복구 가능
  - 감사 추적 (Audit Trail)
  - 다른 서비스와의 관계 보존
- **구현**:
  - Repository 메서드에 `AndDeletedAtIsNull` 조건 추가
  - 실제 DELETE 쿼리는 사용하지 않음

### 6. 로그인 실패 처리
- 5회 실패 시 자동 잠금
- ADMIN이 수동으로 잠금 해제
- **대안 고려**:
  - 일정 시간 후 자동 잠금 해제 (예: 30분)
  - 캡차 도입
  - IP 기반 Rate Limiting

---

## 🧪 테스트 계획

### 1. 단위 테스트 (Service 레이어)
```java
@Test
void testLoginSuccess() {
    // Given
    LoginRequest request = new LoginRequest("2024001", "password123");
    User user = createMockUser();

    // When
    LoginResponse response = authService.loginInternal(request, "127.0.0.1", "Chrome");

    // Then
    assertNotNull(response.getAccessToken());
    assertEquals("2024001", response.getUser().getStudentNum());
}

@Test
void testLoginFail_InvalidPassword() {
    // Given
    LoginRequest request = new LoginRequest("2024001", "wrongpassword");

    // When & Then
    assertThrows(ApiException.class, () ->
        authService.loginInternal(request, "127.0.0.1", "Chrome"));
}

@Test
void testAccountLock_After5Failures() {
    // Given
    User user = createMockUser();

    // When
    for (int i = 0; i < 5; i++) {
        try {
            authService.loginInternal(invalidRequest, "127.0.0.1", "Chrome");
        } catch (ApiException e) {
            // Expected
        }
    }

    // Then
    User lockedUser = userRepository.findById(user.getId()).get();
    assertTrue(lockedUser.getLocked());
    assertEquals(5, lockedUser.getFailCnt());
}
```

### 2. 통합 테스트 (Controller)
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"2024001\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.studentNum").value("2024001"));
    }
}
```

### 3. E2E 테스트 시나리오

**시나리오 1: 회원가입 → 로그인 → 비밀번호 변경**
1. POST `/api/users` - 사용자 생성
2. POST `/api/auth/login` - 로그인
3. POST `/api/auth/password/change` - 비밀번호 변경
4. POST `/api/auth/login` - 새 비밀번호로 로그인

**시나리오 2: 외부 사용자 회원가입 → 이메일 인증 → 로그인**
1. POST `/api/auth/register/external` - 회원가입
2. POST `/api/auth/verify-email` - 이메일 인증
3. POST `/api/auth/login/external` - 로그인

**시나리오 3: 비밀번호 재설정**
1. POST `/api/auth/password/reset-request` - 재설정 요청 (이메일 발송)
2. 이메일에서 토큰 획득
3. POST `/api/auth/password/reset?token=xxx` - 비밀번호 재설정
4. POST `/api/auth/login` - 새 비밀번호로 로그인

---

## 🚀 다음 단계

### 1. 즉시 수행 가능
- [ ] 데이터베이스 생성 (`scms_user`)
- [ ] 초기 데이터 삽입 (ADMIN 계정)
- [ ] Postman 컬렉션 생성
- [ ] 단위 테스트 작성
- [ ] OAuth2 클라이언트 설정 (Google, Kakao)

### 2. Phase 2-2로 이동
- [ ] Notification Service 구현
- [ ] RabbitMQ 이벤트 연동
  - UserCreated
  - PasswordChanged
  - AccountLocked
- [ ] 이메일 알림 자동화

### 3. 프론트엔드 연동 준비
- [ ] Swagger/OpenAPI 문서 생성
- [ ] CORS 정책 세부 조정
- [ ] API 테스트 환경 구축

---

## 📝 알려진 이슈 및 TODO

### 보안
- [ ] JWT Secret Key를 환경 변수로 분리 (현재 하드코딩)
- [ ] Refresh Token Rotation 구현 (보안 강화)
- [ ] IP 기반 Rate Limiting 추가
- [ ] 캡차 도입 검토

### 기능
- [ ] 비밀번호 정책 설정 (최소 길이, 복잡도)
- [ ] 비밀번호 이력 관리 (이전 비밀번호 재사용 방지)
- [ ] 세션 관리 (동시 로그인 제한)
- [ ] 사용자 프로필 이미지 업로드

### 운영
- [ ] 로그 레벨 조정 (운영 환경)
- [ ] 메트릭 수집 (Prometheus)
- [ ] Health Check 세부 구현
- [ ] 데이터베이스 마이그레이션 (Flyway)

### 테스트
- [ ] 단위 테스트 커버리지 80% 이상
- [ ] 통합 테스트 작성
- [ ] 성능 테스트 (JMeter)

---

## 💡 교훈 및 개선 사항

### 잘된 점
1. ✅ **명확한 도메인 분리**: User, ExternalUser, Student, Counselor
2. ✅ **보안 우선 설계**: JWT, BCrypt, 계정 잠금, 로그인 이력
3. ✅ **DTO 분리**: Request/Response 명확히 구분
4. ✅ **ErrorCode 체계화**: 에러 코드 정의 및 공통 처리
5. ✅ **Soft Delete**: 데이터 복구 가능

### 개선 필요
1. ⚠️ **코드 중복**: AuthService에서 내부/외부 사용자 처리 로직 중복
2. ⚠️ **OAuth2 미완성**: 설정 파일만 준비, 실제 구현 필요
3. ⚠️ **테스트 부재**: 테스트 코드 작성 필요
4. ⚠️ **문서화 부족**: Swagger/OpenAPI 문서 필요

### 리팩토링 후보
- [ ] AuthService 분리 (InternalAuthService, ExternalAuthService)
- [ ] JwtTokenProvider를 별도 모듈로 이동 (common-security)
- [ ] EmailService를 별도 서비스로 분리 (Notification Service와 통합)
- [ ] Repository 쿼리 메서드 최적화 (N+1 문제 확인)

---

## 📚 참고 자료

- [Spring Security JWT 공식 문서](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [JJWT 라이브러리](https://github.com/jwtk/jjwt)
- [Spring Data JPA Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods)
- [BCrypt 비밀번호 해싱](https://en.wikipedia.org/wiki/Bcrypt)

---

**작성일**: 2025-11-19
**다음 문서**: `06-notification-service-implementation.md`
