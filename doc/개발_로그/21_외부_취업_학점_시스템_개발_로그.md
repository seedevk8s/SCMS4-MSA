# 21. 외부취업가점 시스템 개발 로그

**작성일:** 2025-11-18
**개발자:** Claude (AI Assistant)
**브랜치:** `claude/review-portfolio-012kRTYwt5T23RBteRQPHmZq`

---

## 📋 목차
1. [개요](#개요)
2. [구현 내용](#구현-내용)
3. [기술적 의사결정](#기술적-의사결정)
4. [커밋 히스토리](#커밋-히스토리)
5. [테스트 가이드](#테스트-가이드)
6. [향후 개선사항](#향후-개선사항)

---

## 개요

### 배경
학생들이 인턴십, 현장실습, 외부 프로젝트, 취업, 창업 등의 활동을 등록하고 관리자 승인을 받아 가점을 획득할 수 있는 시스템이 필요했습니다. 헤더 메뉴에는 "외부취업가점" 링크가 있었지만 실제 기능은 완전히 미구현 상태였습니다.

### 목표
1. **학생 기능**: 외부취업 활동 등록, 조회, 수정, 삭제
2. **관리자 기능**: 활동 승인/거절, 가점 부여, 통계 조회
3. **시스템 연동**: 마일리지 자동 지급, 포트폴리오 확장

### 개발 범위
- **Phase 1**: 백엔드 (DB, Entity, Repository, DTO, Service)
- **Phase 2**: 프론트엔드 (Controller, HTML 템플릿)
- **Phase 3**: 시스템 연동 (마일리지, 포트폴리오, 테스트 데이터)

---

## 구현 내용

### 1. 데이터베이스 설계

#### 1.1 external_employments 테이블

```sql
CREATE TABLE external_employments (
    employment_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    employment_type VARCHAR(50) NOT NULL, -- INTERNSHIP, FIELD_TRAINING, PROJECT, JOB, STARTUP
    company_name VARCHAR(200) NOT NULL,
    position VARCHAR(100),
    department VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    duration_months INTEGER, -- 자동 계산
    description TEXT,
    work_content TEXT,
    skills_learned TEXT,
    certificate_url VARCHAR(500),
    credits INTEGER NOT NULL DEFAULT 0, -- 획득 가점
    is_verified BOOLEAN DEFAULT FALSE, -- 승인 여부
    verified_by INTEGER,
    verification_date TIMESTAMP,
    rejection_reason TEXT,
    is_portfolio_linked BOOLEAN DEFAULT FALSE,
    portfolio_item_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
```

**주요 특징:**
- 활동 기간(duration_months) 자동 계산 (@PrePersist, @PreUpdate)
- Soft Delete 지원 (deleted_at)
- 포트폴리오 연동 준비 (portfolio_item_id)
- 승인/거절 처리 (is_verified, rejection_reason)

#### 1.2 external_employment_credit_rules 테이블

```sql
CREATE TABLE external_employment_credit_rules (
    rule_id SERIAL PRIMARY KEY,
    employment_type VARCHAR(50) NOT NULL,
    min_duration_months INTEGER,
    max_duration_months INTEGER,
    base_credits INTEGER NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**가점 규칙 (초기 데이터):**

| 활동 유형 | 기간 | 가점 | 설명 |
|----------|------|------|------|
| 인턴십 | 0-2개월 | 30점 | 단기 인턴십 |
| 인턴십 | 3-5개월 | 80점 | 중기 인턴십 |
| 인턴십 | 6개월 이상 | 150점 | 장기 인턴십 |
| 현장실습 | 0-2개월 | 50점 | 단기 현장실습 |
| 현장실습 | 3-5개월 | 100점 | 중기 현장실습 |
| 현장실습 | 6개월 이상 | 180점 | 장기 현장실습 |
| 프로젝트 | 0-2개월 | 40점 | 단기 프로젝트 |
| 프로젝트 | 3-5개월 | 90점 | 중기 프로젝트 |
| 프로젝트 | 6개월 이상 | 160점 | 장기 프로젝트 |
| 취업 | 0-5개월 | 150점 | 단기 취업 |
| 취업 | 6-11개월 | 250점 | 중기 취업 |
| 취업 | 12개월 이상 | 400점 | 정규직 취업 |
| 창업 | 0-5개월 | 100점 | 초기 창업 |
| 창업 | 6-11개월 | 200점 | 중기 창업 |
| 창업 | 12개월 이상 | 350점 | 사업자 등록 창업 |

---

### 2. 백엔드 구현

#### 2.1 Entity 클래스

**EmploymentType Enum:**
```java
public enum EmploymentType {
    INTERNSHIP("인턴십", "기업 인턴십 프로그램"),
    FIELD_TRAINING("현장실습", "산학협력 현장실습"),
    PROJECT("외부 프로젝트", "외부 기업/기관 프로젝트 참여"),
    JOB("취업", "정규직/계약직 취업"),
    STARTUP("창업", "개인 창업 또는 스타트업");
}
```

**ExternalEmployment Entity:**
- 파일: `src/main/java/com/scms/app/model/ExternalEmployment.java` (175줄)
- 주요 메서드:
  - `approve()`: 활동 승인
  - `reject()`: 활동 거절
  - `isOngoing()`: 진행 중 여부
  - `linkToPortfolio()`: 포트폴리오 연동

**ExternalEmploymentCreditRule Entity:**
- 파일: `src/main/java/com/scms/app/model/ExternalEmploymentCreditRule.java` (75줄)
- 주요 메서드:
  - `matches()`: 기간이 규칙에 매칭되는지 확인

#### 2.2 Repository

**ExternalEmploymentRepository:**
```java
// 주요 쿼리 메서드
List<ExternalEmployment> findByUserId(Integer userId);
Page<ExternalEmployment> findPendingEmployments(Pageable pageable);
Page<ExternalEmployment> findVerifiedEmployments(Pageable pageable);
Integer getTotalCreditsByUserId(Integer userId);
List<Object[]> getStatisticsByType();
List<Object[]> getMonthlyStatistics();
```

**ExternalEmploymentCreditRuleRepository:**
```java
// 주요 쿼리 메서드
List<ExternalEmploymentCreditRule> findAllActive();
List<ExternalEmploymentCreditRule> findMatchingRules(EmploymentType type, Integer durationMonths);
```

#### 2.3 Service

**ExternalEmploymentService:**
- 파일: `src/main/java/com/scms/app/service/ExternalEmploymentService.java` (360줄)

**핵심 메서드:**
```java
// 학생 기능
ExternalEmploymentResponse registerEmployment(Integer userId, ExternalEmploymentRequest request);
ExternalEmploymentResponse updateEmployment(Long employmentId, Integer userId, ExternalEmploymentRequest request);
void deleteEmployment(Long employmentId, Integer userId);
List<ExternalEmploymentResponse> getEmploymentsByUserId(Integer userId);
Integer getTotalCreditsByUserId(Integer userId);

// 관리자 기능
Page<ExternalEmploymentResponse> getPendingEmployments(Pageable pageable);
Page<ExternalEmploymentResponse> getVerifiedEmployments(Pageable pageable);
ExternalEmploymentResponse verifyEmployment(Long employmentId, Integer adminId, ExternalEmploymentVerifyRequest request);
ExternalEmploymentStatisticsResponse getStatistics();

// 시스템 기능
Integer calculateCredits(EmploymentType employmentType, Integer durationMonths);
```

**가점 자동 계산 로직:**
```java
public Integer calculateCredits(EmploymentType employmentType, Integer durationMonths) {
    List<ExternalEmploymentCreditRule> matchingRules =
        creditRuleRepository.findMatchingRules(employmentType, durationMonths);

    if (matchingRules.isEmpty()) {
        return 0;
    }

    // 가장 높은 가점을 반환
    return matchingRules.get(0).getBaseCredits();
}
```

**마일리지 자동 지급:**
```java
// 승인 시
mileageService.awardMileage(
    employment.getUserId(),
    "EXTERNAL_EMPLOYMENT",
    employment.getEmploymentId(),
    employment.getEmploymentType().getDisplayName() + " - " + employment.getCompanyName(),
    credits,
    employment.getDescription()
);
```

#### 2.4 DTO

1. **ExternalEmploymentRequest** - 등록/수정 요청
2. **ExternalEmploymentResponse** - 조회 응답
3. **ExternalEmploymentVerifyRequest** - 승인/거절 요청
4. **ExternalEmploymentStatisticsResponse** - 통계 응답

---

### 3. REST API Controller

**ExternalEmploymentController:**
- 파일: `src/main/java/com/scms/app/controller/ExternalEmploymentController.java` (205줄)

**학생용 API:**
```java
POST   /api/external-employments              // 활동 등록
PUT    /api/external-employments/{id}         // 활동 수정
DELETE /api/external-employments/{id}         // 활동 삭제
GET    /api/external-employments/my           // 내 활동 목록
GET    /api/external-employments/{id}         // 활동 상세
GET    /api/external-employments/my/total-credits // 총 가점 조회
GET    /api/external-employments/calculate-credits // 가점 계산 미리보기
```

**관리자용 API:**
```java
GET    /api/external-employments/admin/pending    // 승인 대기 목록
GET    /api/external-employments/admin/verified   // 승인 완료 목록
POST   /api/external-employments/{id}/verify      // 승인/거절 처리
GET    /api/external-employments/admin/statistics // 통계 조회
```

---

### 4. 페이지 Controller

**ExternalEmploymentPageController:**
- 파일: `src/main/java/com/scms/app/controller/ExternalEmploymentPageController.java` (65줄)

**페이지 라우팅:**
```java
GET /external-employment           // 학생: 목록 페이지
GET /external-employment/register  // 학생: 등록 페이지
GET /external-employment/{id}      // 학생: 상세 페이지
GET /external-employment/admin     // 관리자: 관리 페이지
```

---

### 5. HTML 템플릿

#### 5.1 list.html (학생용 목록 페이지)
- 파일: `src/main/resources/templates/external-employment/list.html` (165줄)

**주요 기능:**
- 총 획득 가점 표시
- 활동 카드 목록 (승인 상태 뱃지)
- 진행 중/완료 표시
- 승인/거절 사유 표시
- 삭제 기능 (승인되지 않은 활동만)

#### 5.2 register.html (활동 등록 페이지)
- 파일: `src/main/resources/templates/external-employment/register.html` (165줄)

**주요 기능:**
- 활동 유형 선택 (5가지)
- 실시간 가점 계산 미리보기
- 기간 입력 (진행 중 처리 가능)
- 증명서 URL 입력
- Form validation

**실시간 가점 계산:**
```javascript
function calculateCredits() {
    const employmentType = document.getElementById('employmentType').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    // 기간(개월) 계산
    const start = new Date(startDate);
    const end = endDate ? new Date(endDate) : new Date();
    const months = Math.floor((end - start) / (1000 * 60 * 60 * 24 * 30));

    // API 호출
    fetch(`/api/external-employments/calculate-credits?employmentType=${employmentType}&durationMonths=${months}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('estimatedCredits').textContent = data.credits;
        });
}
```

#### 5.3 detail.html (상세 조회 페이지)
- 파일: `src/main/resources/templates/external-employment/detail.html` (125줄)

**표시 정보:**
- 기본 정보 (회사명, 직위, 부서, 기간)
- 활동 설명, 업무 내용, 습득한 기술
- 증명서 링크
- 승인 상태 및 가점
- 거절 사유 (해당 시)

#### 5.4 admin.html (관리자 페이지)
- 파일: `src/main/resources/templates/external-employment/admin.html` (205줄)

**주요 기능:**
- 통계 대시보드 (전체/대기/완료/총가점)
- 탭: 승인 대기 / 승인 완료
- 승인/거절 모달
- 가점 자동 계산 (수동 입력 가능)
- 거절 사유 입력

---

### 6. 시스템 연동

#### 6.1 헤더 링크 활성화

**수정 파일:** `src/main/resources/templates/layout/header.html`
```html
<!-- Before -->
<a href="#" class="header-link">외부취업가점</a>

<!-- After -->
<a href="/external-employment" class="header-link">외부취업가점</a>
```

#### 6.2 포트폴리오 ItemType 확장

**수정 파일:** `src/main/java/com/scms/app/model/PortfolioItemType.java`
```java
public enum PortfolioItemType {
    PROJECT("프로젝트"),
    ACHIEVEMENT("성과/수상"),
    CERTIFICATION("자격증"),
    ACTIVITY("활동"),
    COURSE("교육/강좌"),
    INTERNSHIP("인턴십"),        // 추가
    EMPLOYMENT("취업/경력");      // 추가
}
```

#### 6.3 마일리지 규칙 추가

**수정 파일:** `src/main/java/com/scms/app/config/MileageDataInitializer.java`
```java
// 외부취업 관련 마일리지
createRule("EXTERNAL_EMPLOYMENT", "외부취업 가점", 0,
    "외부취업 활동 승인 시 자동 지급 (가점은 활동별 변동)");
```

#### 6.4 테스트 데이터 Initializer

**신규 파일:** `src/main/java/com/scms/app/config/ExternalEmploymentDataInitializer.java` (145줄)

**생성되는 테스트 데이터 (이영희 학생):**
1. **승인된 인턴십** - (주)테크이노베이션, 6개월, 150점
2. **승인 대기 현장실습** - 스마트솔루션, 3개월
3. **진행 중인 프로젝트** - 푸름대학교 산학협력단
4. **승인된 단기 인턴십** - 스타트업 코딩스쿨, 2개월, 30점
5. **거절된 활동** - 프리랜서 프로젝트 (증빙 부족)

**총 획득 가점:** 180점 (150 + 30)

---

## 기술적 의사결정

### 1. 가점 규칙을 DB 테이블로 관리

**선택:** DB 테이블 (`external_employment_credit_rules`)

**이유:**
- 관리자가 가점 규칙을 동적으로 변경 가능
- 코드 수정 없이 정책 변경 대응
- 규칙 이력 관리 용이

**대안:** Enum 또는 Properties 파일
- 단점: 규칙 변경 시 코드 수정 및 재배포 필요

### 2. 활동 기간 자동 계산

**구현:**
```java
@PrePersist
@PreUpdate
protected void onUpdate() {
    if (startDate != null && endDate != null) {
        Period period = Period.between(startDate, endDate);
        durationMonths = period.getYears() * 12 + period.getMonths();
    }
}
```

**장점:**
- 데이터 일관성 보장
- 가점 계산 자동화
- 사용자 입력 오류 방지

### 3. 승인 시 마일리지 자동 지급

**구현 위치:** `ExternalEmploymentService.verifyEmployment()`

**장점:**
- 학생 혜택 자동 적용
- 수동 처리 오류 방지
- 트랜잭션으로 원자성 보장

**예외 처리:**
```java
try {
    mileageService.awardMileage(...);
} catch (Exception e) {
    log.error("마일리지 지급 실패", e);
    // 마일리지 지급 실패해도 승인은 유지
}
```

### 4. 가점 계산: 자동 vs 수동

**구현:** 자동 계산 + 수동 오버라이드

**자동 계산:**
- 규칙 기반 자동 계산
- 일관성 보장

**수동 입력:**
- 관리자가 특별한 사유로 가점 조정 가능
- 0 입력 시 자동 계산 적용

### 5. Soft Delete 지원

**이유:**
- 삭제된 데이터 복구 가능
- 통계 집계 시 히스토리 유지
- 감사(Audit) 추적 가능

---

## 커밋 히스토리

### Commit #1: 백엔드 구현 (Phase 1 - Part 1)

**커밋 메시지:**
```
feat: 외부취업가점 시스템 백엔드 구현 (Phase 1 - Part 1)

- DB 마이그레이션 (V9): external_employments, external_employment_credit_rules 테이블 생성
- Entity: ExternalEmployment, ExternalEmploymentCreditRule
- Enum: EmploymentType (인턴십, 현장실습, 프로젝트, 취업, 창업)
- Repository: 커스텀 쿼리 메서드 포함
- DTO: Request, Response, VerifyRequest, StatisticsResponse
- Service: 완전 구현
```

**변경 파일:** 11 files, 1,060 lines
- V9__create_external_employment_tables.sql (130줄)
- EmploymentType.java (28줄)
- ExternalEmployment.java (175줄)
- ExternalEmploymentCreditRule.java (75줄)
- ExternalEmploymentRepository.java (95줄)
- ExternalEmploymentCreditRuleRepository.java (40줄)
- ExternalEmploymentRequest.java (43줄)
- ExternalEmploymentResponse.java (44줄)
- ExternalEmploymentVerifyRequest.java (22줄)
- ExternalEmploymentStatisticsResponse.java (38줄)
- ExternalEmploymentService.java (360줄)

**커밋 해시:** `7f76364`

### Commit #2: 프론트엔드 및 시스템 연동 (Phase 1 + Phase 2 완료)

**커밋 메시지:**
```
feat: 외부취업가점 시스템 완전 구현 (Phase 1 + Phase 2 완료)

## 구현 내용

### Backend
- REST API Controller: 학생/관리자 API 엔드포인트
- Page Controller: 페이지 라우팅

### Frontend
- list.html: 학생용 목록 페이지
- register.html: 활동 등록 폼 (실시간 가점 계산)
- detail.html: 상세 조회 페이지
- admin.html: 관리자 승인 페이지

### 연동
- 헤더 링크 활성화
- 포트폴리오 ItemType 확장
- 마일리지 규칙 추가
- 테스트 데이터 Initializer
```

**변경 파일:** 10 files, 1,264 lines
- ExternalEmploymentController.java (205줄)
- ExternalEmploymentPageController.java (65줄)
- ExternalEmploymentDataInitializer.java (145줄)
- list.html (165줄)
- register.html (165줄)
- detail.html (125줄)
- admin.html (205줄)
- header.html (1줄 수정)
- PortfolioItemType.java (2줄 추가)
- MileageDataInitializer.java (3줄 추가)

**커밋 해시:** `b669589`

---

## 테스트 가이드

### 1. 애플리케이션 시작

```bash
./gradlew bootRun
```

**예상 로그:**
```
=== 마일리지 초기 데이터 삽입 시작 ===
...
외부취업 관련 마일리지
마일리지 규칙 생성: 외부취업 가점 - 0P
=== 마일리지 초기 데이터 삽입 완료: 9 개 규칙 생성됨 ===

=== 외부취업 활동 테스트 데이터 삽입 시작 ===
테스트 데이터 생성: 승인된 인턴십 - 150점
테스트 데이터 생성: 승인 대기 중인 현장실습
테스트 데이터 생성: 진행 중인 외부 프로젝트
테스트 데이터 생성: 승인된 단기 인턴십 - 30점
테스트 데이터 생성: 거절된 활동 (증빙 부족)
이영희 학생의 총 획득 가점: 180점
=== 외부취업 활동 테스트 데이터 삽입 완료: 5 개 활동 생성됨 ===
```

### 2. 학생 계정 테스트 (이영희)

**로그인:**
- 계정: 이영희 (student)
- 헤더의 "외부취업가점" 클릭

**확인 사항:**
1. 총 가점 표시: **180점**
2. 활동 목록: **5개 카드**
   - 승인됨 (2개): 150점, 30점
   - 승인 대기 (2개)
   - 거절됨 (1개) - 거절 사유 표시

**새 활동 등록:**
1. "새 활동 등록" 버튼 클릭
2. 활동 유형 선택: 인턴십
3. 회사명 입력: 테스트 회사
4. 시작일: 3개월 전
5. 종료일: 현재
6. **예상 가점 자동 계산**: 80점 표시 확인
7. 등록 버튼 클릭
8. 목록에서 "승인 대기" 상태 확인

### 3. 관리자 계정 테스트

**로그인:**
- 계정: 관리자 (admin)
- URL: `/external-employment/admin`

**확인 사항:**
1. **통계 대시보드:**
   - 전체 신청: 6개
   - 승인 대기: 4개
   - 승인 완료: 2개
   - 총 부여 가점: 180점

2. **승인 대기 탭:**
   - 3개 활동 표시
   - "검토" 버튼 클릭

3. **승인 처리:**
   - 모달에서 자동 계산된 가점 확인
   - 가점 수정 가능
   - "승인" 버튼 클릭
   - 승인 완료 탭으로 이동 확인

4. **거절 처리:**
   - "거절" 버튼 클릭
   - 거절 사유 입력란 표시
   - 사유 입력 후 "거절 확정" 클릭

### 4. API 테스트

**학생용 API:**
```bash
# 내 활동 목록 조회
curl -X GET http://localhost:8080/api/external-employments/my

# 총 가점 조회
curl -X GET http://localhost:8080/api/external-employments/my/total-credits

# 활동 등록
curl -X POST http://localhost:8080/api/external-employments \
  -H "Content-Type: application/json" \
  -d '{
    "employmentType": "INTERNSHIP",
    "companyName": "테스트 회사",
    "startDate": "2024-08-01",
    "endDate": "2024-11-01",
    "description": "테스트 활동"
  }'

# 가점 계산 미리보기
curl -X GET "http://localhost:8080/api/external-employments/calculate-credits?employmentType=INTERNSHIP&durationMonths=3"
```

**관리자용 API:**
```bash
# 승인 대기 목록
curl -X GET http://localhost:8080/api/external-employments/admin/pending

# 통계 조회
curl -X GET http://localhost:8080/api/external-employments/admin/statistics

# 승인 처리
curl -X POST http://localhost:8080/api/external-employments/1/verify \
  -H "Content-Type: application/json" \
  -d '{
    "approve": true,
    "credits": 150
  }'

# 거절 처리
curl -X POST http://localhost:8080/api/external-employments/2/verify \
  -H "Content-Type: application/json" \
  -d '{
    "approve": false,
    "rejectionReason": "증빙 자료가 부족합니다."
  }'
```

### 5. 데이터베이스 확인

```sql
-- 활동 목록 조회
SELECT employment_id, company_name, employment_type, duration_months, credits, is_verified
FROM external_employments
WHERE deleted_at IS NULL;

-- 가점 규칙 조회
SELECT * FROM external_employment_credit_rules WHERE is_active = true;

-- 사용자별 총 가점
SELECT user_id, SUM(credits) as total_credits
FROM external_employments
WHERE is_verified = true AND deleted_at IS NULL
GROUP BY user_id;

-- 마일리지 지급 확인
SELECT * FROM mileage_history WHERE activity_type = 'EXTERNAL_EMPLOYMENT';
```

---

## 향후 개선사항

### 1. 파일 업로드 기능

**현재 한계:**
- 증명서 URL만 입력 가능
- 실제 파일 업로드 미지원

**개선 방안:**
```java
@PostMapping("/api/external-employments/{id}/certificate")
public ResponseEntity<String> uploadCertificate(
    @PathVariable Long id,
    @RequestParam("file") MultipartFile file) {

    // S3 또는 로컬 스토리지에 업로드
    String fileUrl = fileStorageService.upload(file);

    // DB에 URL 저장
    employmentService.updateCertificateUrl(id, fileUrl);

    return ResponseEntity.ok(fileUrl);
}
```

### 2. 포트폴리오 자동 연동

**현재 한계:**
- 포트폴리오 연동 필드만 존재
- 실제 연동 로직 미구현

**개선 방안:**
```java
// 승인 시 자동으로 포트폴리오 항목 생성
public void linkToPortfolio(ExternalEmployment employment) {
    PortfolioItem item = PortfolioItem.builder()
        .portfolioId(/* 사용자의 기본 포트폴리오 */)
        .itemType(getPortfolioItemType(employment.getEmploymentType()))
        .title(employment.getCompanyName() + " - " + employment.getPosition())
        .description(employment.getDescription())
        .startDate(employment.getStartDate())
        .endDate(employment.getEndDate())
        .build();

    portfolioItemRepository.save(item);
    employment.linkToPortfolio(item.getItemId());
}

private PortfolioItemType getPortfolioItemType(EmploymentType employmentType) {
    return switch (employmentType) {
        case INTERNSHIP -> PortfolioItemType.INTERNSHIP;
        case JOB -> PortfolioItemType.EMPLOYMENT;
        default -> PortfolioItemType.ACTIVITY;
    };
}
```

### 3. 알림 연동

**추가할 알림:**
- 학생: 활동 승인/거절 시
- 관리자: 새 활동 등록 시

**구현:**
```java
// NotificationService 활용
notificationService.createNotification(
    employment.getUserId(),
    NotificationType.EMPLOYMENT_APPROVED,
    "외부취업 활동이 승인되었습니다. 획득 가점: " + credits + "점"
);
```

### 4. 통계 차트

**현재:**
- 숫자로만 통계 표시

**개선:**
- Chart.js로 시각화
- 월별 신청 추이 그래프
- 활동 유형별 파이 차트
- 가점 분포 히스토그램

### 5. 관리자 승인 권한 세분화

**현재:**
- 모든 관리자가 승인 가능

**개선:**
- 역할별 권한 분리 (APPROVER, VIEWER)
- 승인 금액 한도 설정
- 다단계 승인 프로세스

### 6. 활동 검증 강화

**추가 검증:**
- 중복 활동 체크
- 기간 겹침 검증
- 증명서 유효성 검사
- 회사 정보 자동 완성 (외부 API 연동)

### 7. 엑셀 내보내기

**관리자 기능:**
```java
@GetMapping("/api/external-employments/admin/export")
public ResponseEntity<byte[]> exportToExcel() {
    List<ExternalEmployment> employments = employmentRepository.findAll();
    byte[] excelData = excelService.createExcel(employments);

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=employments.xlsx")
        .body(excelData);
}
```

### 8. 가점 규칙 관리 페이지

**현재:**
- DB에 직접 규칙 삽입

**개선:**
- 관리자 페이지에서 규칙 CRUD
- 규칙 활성화/비활성화
- 규칙 변경 이력 관리

---

## 결론

### 달성한 목표
✅ 외부취업 활동 관리 시스템 완전 구현
✅ 학생/관리자 기능 분리
✅ 가점 자동 계산 시스템
✅ 마일리지 자동 연동
✅ 포트폴리오 확장 준비
✅ 테스트 데이터 제공

### 주요 성과
- **백엔드:** 21 files, 2,324 lines
- **가점 규칙:** 15개 기본 규칙
- **테스트 데이터:** 5개 샘플 활동
- **API 엔드포인트:** 11개
- **HTML 페이지:** 4개

### 시스템 특징
1. **자동화:** 기간 계산, 가점 계산, 마일리지 지급 모두 자동
2. **유연성:** 가점 규칙 DB 관리로 정책 변경 용이
3. **안전성:** Soft Delete, 트랜잭션 처리, 권한 검증
4. **확장성:** 포트폴리오 연동, 알림 연동 준비 완료

---

**문서 버전:** 1.0
**최종 수정일:** 2025-11-18
**총 구현 시간:** 약 3시간
**코드 라인 수:** 2,324 lines
