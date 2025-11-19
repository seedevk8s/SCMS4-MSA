# 🚀 SCMS4-MSA 빠른 시작 가이드

## 📋 목차
1. [사전 요구사항](#사전-요구사항)
2. [프로젝트 설정](#프로젝트-설정)
3. [서비스 실행](#서비스-실행)
4. [초기 데이터 설정](#초기-데이터-설정)
5. [API 테스트](#api-테스트)
6. [문제 해결](#문제-해결)

---

## 사전 요구사항

### 필수 설치
- ☑️ **Java 17 이상**
  ```bash
  java -version
  # 17 이상이어야 함
  ```

- ☑️ **MySQL 8.0 이상**
  ```bash
  mysql --version
  ```

- ☑️ **Git**
  ```bash
  git --version
  ```

### 선택 사항
- IntelliJ IDEA (추천)
- Postman (API 테스트)
- Docker (향후 컨테이너화)

---

## 프로젝트 설정

### 1. 저장소 클론

```bash
# HTTPS
git clone https://github.com/seedevk8s/SCMS4-MSA.git
cd SCMS4-MSA

# SSH
git clone git@github.com:seedevk8s/SCMS4-MSA.git
cd SCMS4-MSA

# 특정 브랜치 (개발 중인 경우)
git checkout claude/monolith-to-microservices-01HjHjwhpzvWp5jeqfSUJc1x
```

### 2. MySQL 데이터베이스 생성

```sql
-- MySQL 접속
mysql -u root -p

-- 데이터베이스 생성
CREATE DATABASE scms_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 권한 확인
SHOW GRANTS FOR 'root'@'localhost';

-- 종료
exit;
```

### 3. 환경 설정 (선택사항)

#### application.yml 확인/수정

**데이터베이스 설정:**
```yaml
# services/user-service/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scms_user?useSSL=false&serverTimezone=Asia/Seoul
    username: root
    password: password  # 본인의 MySQL 비밀번호로 변경
```

**이메일 설정 (비밀번호 재설정 기능 사용 시):**
```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-app-password  # Gmail 앱 비밀번호
```

> 💡 이메일 설정 없이도 서비스는 정상 실행됩니다. 비밀번호 재설정 기능만 동작하지 않습니다.

---

## 서비스 실행

### ⚡ 방법 1: IntelliJ IDEA (추천)

#### 1. 프로젝트 열기
```
File → Open → SCMS4-MSA 폴더 선택
```

#### 2. Gradle 동기화 대기
- 우측 하단 "Indexing..." 완료까지 대기 (1~2분)
- Gradle 의존성 자동 다운로드

#### 3. 서비스 순차 실행

**① Eureka Server (필수) - 포트 8761**
```
infrastructure/eureka-server/src/main/java/com/scms/eureka/EurekaServerApplication.java
→ 우클릭 → Run 'EurekaServerApplication'
```
- 로그에서 "Started EurekaServerApplication" 확인
- 브라우저: http://localhost:8761

**② API Gateway (필수) - 포트 8080**
```
infrastructure/api-gateway/src/main/java/com/scms/gateway/ApiGatewayApplication.java
→ 우클릭 → Run 'ApiGatewayApplication'
```
- 로그에서 "Started ApiGatewayApplication" 확인

**③ User Service (필수) - 포트 8081**
```
services/user-service/src/main/java/com/scms/user/UserServiceApplication.java
→ 우클릭 → Run 'UserServiceApplication'
```
- 로그에서 "Started UserServiceApplication" 확인
- JPA가 테이블 자동 생성 (DDL 로그 확인)

#### 4. 실행 확인

브라우저에서 확인:
- http://localhost:8761 - Eureka Dashboard (USER-SERVICE 등록 확인)
- http://localhost:8081/actuator/health - User Service Health Check

---

### 🖥 방법 2: 터미널 (Linux/macOS)

```bash
cd SCMS4-MSA

# 1. Eureka Server 실행 (백그라운드)
./gradlew :infrastructure:eureka-server:bootRun &
sleep 3

# 2. API Gateway 실행 (백그라운드)
./gradlew :infrastructure:api-gateway:bootRun &
sleep 3

# 3. User Service 실행 (포그라운드)
./gradlew :services:user-service:bootRun

# Ctrl+C로 종료
```

**백그라운드 프로세스 종료:**
```bash
# Java 프로세스 확인
jps -l

# 프로세스 종료
kill <PID>
```

---

### 🪟 방법 3: 터미널 (Windows)

각각 **새 PowerShell 창**에서 실행:

```powershell
# 창 1: Eureka Server
cd SCMS4-MSA
.\gradlew.bat :infrastructure:eureka-server:bootRun

# 창 2: API Gateway
cd SCMS4-MSA
.\gradlew.bat :infrastructure:api-gateway:bootRun

# 창 3: User Service
cd SCMS4-MSA
.\gradlew.bat :services:user-service:bootRun
```

---

## 초기 데이터 설정

### ADMIN 계정 생성

서비스가 실행되면 테이블이 자동 생성됩니다. ADMIN 계정을 수동으로 추가하세요:

```sql
-- MySQL 접속
mysql -u root -p
USE scms_user;

-- ADMIN 사용자 생성 (비밀번호: admin123!@#)
INSERT INTO users (
    student_num,
    password,
    email,
    name,
    phone,
    role,
    locked,
    fail_cnt,
    password_updated_at,
    created_at,
    updated_at
) VALUES (
    'ADMIN',
    '$2a$10$XPTYhEqQx.TvhCJKqV7zYuD8bLQPqJGz7cqVX0nN7wvHdUh6ZQZPm',
    'admin@scms.com',
    '시스템 관리자',
    '010-0000-0000',
    'ADMIN',
    false,
    0,
    NOW(),
    NOW(),
    NOW()
);

-- 확인
SELECT user_id, student_num, name, email, role FROM users;
```

**로그인 정보:**
- 학번: `ADMIN`
- 비밀번호: `admin123!@#`

---

## API 테스트

### ✅ Health Check

```bash
curl http://localhost:8081/actuator/health
```

**응답:**
```json
{
  "status": "UP"
}
```

---

### ✅ 로그인 (ADMIN)

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginId": "ADMIN",
    "password": "admin123!@#"
  }'
```

**성공 응답:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userType": "INTERNAL",
  "user": {
    "userId": 1,
    "studentNum": "ADMIN",
    "email": "admin@scms.com",
    "name": "시스템 관리자",
    "role": "ADMIN"
  }
}
```

---

### ✅ 사용자 생성 (ADMIN 권한 필요)

```bash
# 1. 로그인하여 토큰 저장
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginId":"ADMIN","password":"admin123!@#"}' \
  | jq -r '.accessToken')

# 2. 학생 사용자 생성
curl -X POST http://localhost:8081/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studentNum": "2024001",
    "password": "student123!@#",
    "confirmPassword": "student123!@#",
    "email": "student@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "role": "STUDENT"
  }'
```

---

### ✅ 학번 중복 체크 (Public)

```bash
curl http://localhost:8081/api/users/check/student-num?studentNum=2024001
```

**응답:**
```json
{
  "available": true  // true = 사용 가능, false = 중복
}
```

---

### 📮 Postman 사용

1. **Postman 설치**: https://www.postman.com/downloads/
2. **컬렉션 Import**:
   - Postman 실행
   - Import → File → `postman/User-Service-API.postman_collection.json` 선택
3. **로그인 요청 실행** → accessToken 복사
4. **환경 변수 설정** (Collection Variables):
   - `accessToken`: 로그인 응답의 accessToken 붙여넣기
   - `refreshToken`: 로그인 응답의 refreshToken 붙여넣기
5. **다른 API 테스트** (자동으로 토큰 사용)

---

## 문제 해결

### ❌ "Port 8081 already in use"

```bash
# 포트 사용 중인 프로세스 확인
# Linux/macOS
lsof -i :8081

# Windows
netstat -ano | findstr :8081

# 프로세스 종료
kill -9 <PID>  # Linux/macOS
taskkill /PID <PID> /F  # Windows
```

---

### ❌ "Access denied for user 'root'@'localhost'"

**application.yml 수정:**
```yaml
spring:
  datasource:
    username: root
    password: YOUR_MYSQL_PASSWORD  # 본인의 MySQL 비밀번호
```

또는 MySQL 사용자 생성:
```sql
CREATE USER 'scms'@'localhost' IDENTIFIED BY 'scms123';
GRANT ALL PRIVILEGES ON scms_user.* TO 'scms'@'localhost';
FLUSH PRIVILEGES;
```

---

### ❌ "Table 'scms_user.users' doesn't exist"

서비스를 한 번 실행하면 JPA가 자동으로 테이블을 생성합니다.

로그 확인:
```
Hibernate: create table users (...)
```

수동으로 확인:
```sql
USE scms_user;
SHOW TABLES;
```

---

### ❌ Eureka Dashboard에 서비스가 안 보임

1. **Eureka Server 먼저 실행 확인**
   - http://localhost:8761 접속 가능 확인

2. **User Service 로그 확인**
   ```
   DiscoveryClient_USER-SERVICE/... - registration status: 204
   ```

3. **30초~1분 대기** (등록 갱신 주기)

4. **방화벽 확인**
   ```bash
   # 포트 열기 확인
   telnet localhost 8761
   telnet localhost 8081
   ```

---

### ❌ "Failed to send email"

이메일 발송 실패는 서비스 실행에 영향을 주지 않습니다.

**해결 방법:**
1. Gmail 앱 비밀번호 생성 (2단계 인증 필수)
2. application.yml에 올바른 이메일/비밀번호 설정
3. 환경 변수 사용:
   ```bash
   export MAIL_USERNAME=your-email@gmail.com
   export MAIL_PASSWORD=your-app-password
   ```

---

## 📚 다음 단계

### API 문서
- **Swagger UI** (예정): http://localhost:8081/swagger-ui.html
- **상세 문서**: `doc/msa-migration/05-user-service-implementation.md`

### 추가 서비스 실행
- Notification Service (예정)
- Program Service (예정)

### 개발 환경 설정
- **Hot Reload**: IntelliJ에서 "Build → Build Project" 시 자동 재시작
- **디버깅**: Breakpoint 설정 후 Debug 모드로 실행

---

## 🆘 도움말

### 로그 확인

**IntelliJ:**
- Run 탭에서 실시간 로그 확인

**터미널:**
```bash
# User Service 로그 레벨 조정
# application.yml
logging:
  level:
    com.scms.user: DEBUG
    org.springframework.security: DEBUG
```

### 데이터베이스 확인

```sql
-- 모든 테이블 확인
USE scms_user;
SHOW TABLES;

-- 사용자 목록
SELECT * FROM users;

-- 로그인 이력
SELECT * FROM login_history ORDER BY login_at DESC LIMIT 10;

-- 테이블 구조
DESCRIBE users;
```

### 서비스 재시작

**IntelliJ:**
- Stop 버튼 클릭 후 다시 Run

**터미널:**
- `Ctrl+C`로 종료 후 재실행

---

## ✅ 정상 실행 확인

모든 것이 정상이면:

1. ✅ Eureka Dashboard (http://localhost:8761)에 **USER-SERVICE** 표시
2. ✅ Health Check API 응답 `{"status":"UP"}`
3. ✅ 로그인 API 호출 시 JWT 토큰 발급
4. ✅ MySQL에 `users`, `students`, `login_history` 등 테이블 생성

**축하합니다! 🎉 User Service가 성공적으로 실행되었습니다.**

---

## 📞 문의

- **GitHub Issues**: https://github.com/seedevk8s/SCMS4-MSA/issues
- **문서**: `doc/msa-migration/`
- **API 명세**: `doc/msa-migration/05-user-service-implementation.md`

---

**Last Updated**: 2025-11-19
**Version**: Phase 2-1 (User Service)
