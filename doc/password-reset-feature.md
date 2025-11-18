# 비밀번호 찾기/재설정 기능 구현

## 📋 개요

사용자가 비밀번호를 잊어버렸을 때 안전하게 비밀번호를 재설정할 수 있는 기능을 구현했습니다.

## 🎯 구현 기능

### 1. 내부 회원 (학생) 비밀번호 재설정

#### 방법 1: 학번 + 이름 + 생년월일 방식 (즉시 초기화)
- **경로**: `/password/reset`
- **API**: `POST /api/auth/password/reset`
- **동작**:
  - 학번, 이름, 생년월일로 본인 확인
  - 비밀번호를 생년월일 6자리(YYMMDD)로 즉시 초기화
  - 계정 잠금 자동 해제

#### 방법 2: 이메일을 통한 토큰 기반 재설정 (안전)
- **API**: `POST /api/auth/password/reset-request`
- **동작**:
  1. 이메일 주소 입력
  2. 등록된 이메일로 재설정 링크 발송 (1시간 유효)
  3. 이메일의 링크 클릭
  4. 새 비밀번호 입력 및 설정

### 2. 외부 회원 비밀번호 재설정

#### 이메일을 통한 토큰 기반 재설정
- **API**: `POST /api/auth/password/reset-request`
- **동작**: 내부 회원과 동일한 방식
- 외부 회원은 이메일로만 로그인하므로 이메일 기반 재설정만 지원

### 3. 토큰 기반 비밀번호 재설정 프로세스

#### 사용자 흐름
1. **재설정 요청**: 이메일 주소 입력
2. **이메일 발송**: 비밀번호 재설정 링크 발송 (1시간 유효)
3. **토큰 검증**: 링크 클릭 시 토큰 유효성 자동 확인
4. **비밀번호 변경**: 새 비밀번호 입력 및 확인
5. **완료**: 로그인 페이지로 자동 이동

## 🗂 구현 파일

### 백엔드

#### 엔티티
- `PasswordResetToken.java`: 비밀번호 재설정 토큰 관리
  - 토큰 문자열 (UUID)
  - 토큰 타입 (INTERNAL/EXTERNAL)
  - 사용자 참조 (User/ExternalUser)
  - 만료 시간 (1시간)
  - 사용 여부 추적

#### Repository
- `PasswordResetTokenRepository.java`: 토큰 관리 저장소
  - 유효한 토큰 조회
  - 만료된 토큰 자동 삭제
  - 사용자별 토큰 무효화

#### Service
- `UserService.java`:
  - `requestPasswordResetByEmail()`: 내부 회원 재설정 요청
  - `resetPasswordWithToken()`: 토큰 기반 비밀번호 변경
  - `validateResetToken()`: 토큰 유효성 검증
  - `cleanupExpiredTokens()`: 만료된 토큰 정리

- `ExternalUserService.java`:
  - `requestPasswordResetByEmail()`: 외부 회원 재설정 요청
  - `resetPasswordWithToken()`: 토큰 기반 비밀번호 변경

- `EmailService.java`:
  - `sendPasswordResetEmail()`: 비밀번호 재설정 이메일 발송

#### Controller
- `AuthController.java`:
  - `POST /api/auth/password/reset`: 학번 기반 즉시 초기화
  - `POST /api/auth/password/reset-request`: 이메일 재설정 요청
  - `POST /api/auth/password/reset-with-token`: 토큰 기반 비밀번호 변경
  - `GET /api/auth/password/validate-token`: 토큰 유효성 검증

- `HomeController.java`:
  - `GET /password/reset`: 비밀번호 찾기 페이지
  - `GET /password/reset-with-token`: 토큰 기반 재설정 페이지
  - `GET /external/reset-password`: 외부 회원 재설정 페이지

### 프론트엔드

#### 페이지
- `password-reset.html`: 비밀번호 찾기 페이지
  - 학번 + 이름 + 생년월일 입력 폼
  - 즉시 초기화 기능

- `password-reset-with-token.html`: 토큰 기반 비밀번호 재설정 페이지
  - 토큰 자동 검증
  - 사용자 정보 표시
  - 새 비밀번호 입력 폼
  - 비밀번호 요구사항 안내

#### 이메일 템플릿
- `email/password-reset.html`: 비밀번호 재설정 이메일 템플릿
  - 반응형 디자인
  - 재설정 버튼
  - 보안 안내 메시지
  - 링크 만료 시간 안내

## 🔒 보안 기능

### 1. 토큰 보안
- **UUID 기반 토큰**: 예측 불가능한 고유 토큰 생성
- **만료 시간**: 1시간 후 자동 만료
- **일회용 토큰**: 사용 후 자동 무효화
- **토큰 무효화**: 새 토큰 발급 시 기존 토큰 무효화

### 2. 이메일 보안
- **이메일 존재 여부 숨김**: 등록되지 않은 이메일도 동일한 응답 반환 (보안상 이유)
- **안전한 링크**: HTTPS를 통한 안전한 재설정 링크 전송

### 3. 계정 보안
- **계정 잠금 해제**: 비밀번호 재설정 시 자동으로 계정 잠금 해제
- **실패 횟수 초기화**: 로그인 실패 횟수 초기화

### 4. 데이터 검증
- **비밀번호 길이**: 최소 6자 이상
- **비밀번호 일치 확인**: 새 비밀번호와 확인 비밀번호 일치 여부 검증
- **입력 유효성 검사**: 모든 필수 필드 검증

## 📊 데이터베이스 스키마

### password_reset_tokens 테이블
```sql
CREATE TABLE password_reset_tokens (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(20) NOT NULL,  -- INTERNAL / EXTERNAL
    user_id INT,
    external_user_id INT,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (external_user_id) REFERENCES external_users(user_id)
);
```

### 인덱스
- `token` (UNIQUE): 빠른 토큰 조회
- `email`: 이메일 기반 조회 최적화
- `expires_at`: 만료된 토큰 정리 최적화

## 🔄 프로세스 흐름도

### 이메일 기반 비밀번호 재설정
```
1. 사용자: 이메일 입력 → POST /api/auth/password/reset-request

2. 서버:
   ├─ 이메일로 사용자 조회
   ├─ 기존 토큰 무효화
   ├─ 새 토큰 생성 (UUID, 1시간 만료)
   ├─ 토큰 저장
   └─ 이메일 발송 (재설정 링크 포함)

3. 사용자: 이메일의 링크 클릭 → GET /password/reset-with-token?token=xxx

4. 페이지 로드:
   └─ GET /api/auth/password/validate-token?token=xxx
      ├─ 토큰 검증
      └─ 사용자 정보 반환

5. 사용자: 새 비밀번호 입력 → POST /api/auth/password/reset-with-token

6. 서버:
   ├─ 토큰 유효성 재검증
   ├─ 비밀번호 암호화
   ├─ 비밀번호 업데이트
   ├─ 토큰 사용 처리
   └─ 계정 잠금 해제

7. 완료: 로그인 페이지로 이동
```

## 🧪 테스트 시나리오

### 1. 정상 흐름 테스트
- [ ] 학번 기반 비밀번호 초기화 성공
- [ ] 이메일 기반 재설정 링크 발송 성공
- [ ] 토큰 유효성 검증 성공
- [ ] 새 비밀번호 설정 성공
- [ ] 로그인 성공

### 2. 에러 처리 테스트
- [ ] 존재하지 않는 학번/이메일 처리
- [ ] 만료된 토큰 처리
- [ ] 사용된 토큰 재사용 방지
- [ ] 비밀번호 불일치 처리
- [ ] 비밀번호 길이 부족 처리

### 3. 보안 테스트
- [ ] 토큰 예측 불가능성 확인
- [ ] 이메일 존재 여부 추측 불가 확인
- [ ] HTTPS 강제 확인
- [ ] XSS/CSRF 공격 방어 확인

## 📈 향후 개선 사항

### 기능 개선
1. **SMS 인증 추가**: 이메일 외에 SMS로도 재설정 가능
2. **2단계 인증**: 추가 보안 레이어
3. **비밀번호 강도 체크**: 더 강력한 비밀번호 요구사항
4. **재설정 횟수 제한**: Rate Limiting 구현

### UI/UX 개선
1. **비밀번호 찾기 페이지 탭**: 학번/이메일 선택 탭 추가
2. **진행 상태 표시**: 재설정 프로세스 단계별 표시
3. **실시간 유효성 검사**: 입력 필드 실시간 검증
4. **다국어 지원**: 영어 등 다국어 지원

### 운영 개선
1. **만료 토큰 자동 정리**: 스케줄러 추가
2. **재설정 이력 로깅**: 감사 로그 추가
3. **알림 기능**: 비밀번호 변경 시 알림
4. **통계 대시보드**: 재설정 요청 통계

## 🚀 배포 시 주의사항

### 환경 변수 설정
```properties
# 이메일 설정
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# 서버 도메인 (이메일 링크에 사용)
server.domain=https://your-domain.com
```

### 데이터베이스 마이그레이션
- JPA가 자동으로 `password_reset_tokens` 테이블 생성
- 또는 수동으로 테이블 생성 스크립트 실행

### 보안 설정
- HTTPS 강제 활성화
- CSRF 보호 활성화 (현재 비활성화 상태)
- Rate Limiting 설정

## 📝 API 문서

### POST /api/auth/password/reset
학번 기반 비밀번호 즉시 초기화

**Request:**
```json
{
  "studentNum": 2024001,
  "name": "김철수",
  "birthDate": "2003-01-01"
}
```

**Response:**
```
비밀번호가 초기화되었습니다. 생년월일(6자리)로 로그인해주세요.
```

### POST /api/auth/password/reset-request
이메일로 비밀번호 재설정 요청

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```
비밀번호 재설정 링크가 이메일로 발송되었습니다. 이메일을 확인해주세요.
```

### POST /api/auth/password/reset-with-token
토큰 기반 비밀번호 변경

**Request:**
```json
{
  "token": "uuid-token-string",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

**Response:**
```
비밀번호가 성공적으로 변경되었습니다. 로그인해주세요.
```

### GET /api/auth/password/validate-token
토큰 유효성 검증

**Request:**
```
GET /api/auth/password/validate-token?token=uuid-token-string
```

**Response:**
```json
{
  "valid": true,
  "email": "user@example.com",
  "name": "김철수"
}
```

## 👥 작성자
- Claude AI Assistant
- 날짜: 2025-11-18
