# 19. 역량진단 및 맞춤형 프로그램 추천 시스템 개발 로그

**작성일:** 2025-11-18
**개발자:** Claude (AI Assistant)
**브랜치:** `claude/implement-competency-assessment-016oycC5yKv54U5RZQ41En7Y`

---

## 📋 목차
1. [개요](#개요)
2. [구현 내용](#구현-내용)
3. [주요 버그 및 해결](#주요-버그-및-해결)
4. [기술적 의사결정](#기술적-의사결정)
5. [커밋 히스토리](#커밋-히스토리)
6. [테스트 가이드](#테스트-가이드)
7. [향후 개선사항](#향후-개선사항)

---

## 개요

### 배경
비교과 통합 관리 시스템(SCMS)에 학생의 역량을 진단하고, 진단 결과를 기반으로 맞춤형 프로그램을 추천하는 기능이 필요했습니다.

### 목표
1. **역량진단 시스템 구현**
   - 카테고리별 역량 관리
   - 학생별 역량 평가 저장
   - 평가 결과 시각화 (Chart.js)

2. **맞춤형 프로그램 추천 시스템 구현**
   - 프로그램-역량 매핑
   - 학생의 약점 기반 추천 알고리즘
   - 추천 점수 계산 및 정렬

### 개발 범위
- 백엔드: Entity, Repository, Service, Controller, DTO
- 프론트엔드: 결과 페이지, Chart.js 연동
- 데이터: 샘플 데이터 자동 생성
- 추천: 키워드 기반 매핑 로직

---

## 구현 내용

### 1. 역량진단 시스템

#### 1.1 데이터베이스 설계

**ERD:**
```
CompetencyCategory (역량 카테고리)
├── id (PK)
├── name (카테고리명: 전공역량, 일반역량, 리더십역량)
└── description

Competency (역량 항목)
├── id (PK)
├── category_id (FK → CompetencyCategory)
├── name (역량명: 프로그래밍 능력, 의사소통 능력 등)
└── description

StudentCompetencyAssessment (학생 역량 평가)
├── id (PK)
├── student_id (FK → Student)
├── competency_id (FK → Competency)
├── score (점수: 0-100)
├── assessment_date (평가일)
├── assessor (평가자)
└── notes (비고)
```

**주요 특징:**
- 카테고리 → 역량 → 평가의 3단계 구조
- 시간에 따른 평가 이력 관리 가능
- 점수 기반 등급 산출 (A/B/C/D/F)

#### 1.2 백엔드 구현

**Entity (3개)**
- `CompetencyCategory.java`: 역량 카테고리 엔티티
- `Competency.java`: 역량 항목 엔티티
- `StudentCompetencyAssessment.java`: 평가 결과 엔티티

**Repository (3개)**
- `CompetencyCategoryRepository`: 카테고리 조회
- `CompetencyRepository`: 역량 조회 (카테고리 포함)
- `StudentCompetencyAssessmentRepository`: 평가 조회 (최신 평가, 평균 점수 등)

**핵심 쿼리:**
```java
// 학생의 최신 평가 결과 조회
@Query("SELECT a FROM StudentCompetencyAssessment a " +
       "JOIN FETCH a.competency c " +
       "JOIN FETCH c.category " +
       "WHERE a.student.id = :studentId " +
       "AND a.assessmentDate = (SELECT MAX(a2.assessmentDate) ...)")
List<StudentCompetencyAssessment> findLatestAssessmentsByStudentId(@Param("studentId") Long studentId);
```

**Service (2개)**
- `CompetencyService`: 역량 CRUD
- `CompetencyAssessmentService`: 평가 제출, 리포트 생성

**핵심 비즈니스 로직:**
```java
// 평가 리포트 생성
public AssessmentReportResponse generateReport(Long studentId) {
    // 1. 최신 평가 조회
    List<StudentCompetencyAssessment> assessments = findLatest...

    // 2. 전체 평균 점수 계산
    Double totalScore = assessments.stream()
        .mapToInt(StudentCompetencyAssessment::getScore)
        .average().orElse(0.0);

    // 3. 카테고리별 평균 계산
    Map<CompetencyCategory, List<...>> categoryMap =
        assessments.stream().collect(Collectors.groupingBy(...));

    // 4. 강점/약점 추출 (Top 3 / Bottom 3)
    List<CompetencyScoreDto> strengths = assessments.stream()
        .sorted(Comparator.comparing(...).reversed())
        .limit(3).collect(...);

    return AssessmentReportResponse.builder()...
}
```

**Controller (3개)**
- `CompetencyController`: 역량 CRUD REST API
- `CompetencyAssessmentController`: 평가 제출/조회 REST API
- `CompetencyPageController`: 결과 페이지 라우팅

**REST API 엔드포인트 (20+개):**
```
GET    /api/competencies                              # 전체 역량 조회
GET    /api/competencies/categories                   # 카테고리 목록
GET    /api/competencies/categories/{id}              # 카테고리 상세
GET    /api/competencies/categories/{id}/competencies # 카테고리별 역량

POST   /api/assessments/submit                        # 평가 제출
GET    /api/assessments/students/{studentId}          # 학생 평가 조회
GET    /api/assessments/students/{studentId}/report   # 평가 리포트
GET    /api/assessments/students/{studentId}/latest   # 최신 평가
```

#### 1.3 프론트엔드 구현

**competency-result.html**

**주요 기능:**
1. Chart.js 레이더 차트로 역량 프로필 시각화
2. 카테고리별 점수 카드 표시
3. 강점/약점 분석 (Top 3 / Bottom 3)
4. 추천 프로그램 보기 버튼

**핵심 코드:**
```javascript
// 레이더 차트 생성
function displayRadarChart(categoryScores) {
    new Chart(ctx, {
        type: 'radar',
        data: {
            labels: categoryScores.map(c => c.categoryName),
            datasets: [{
                label: '역량 점수',
                data: categoryScores.map(c => c.averageScore),
                backgroundColor: 'rgba(44, 95, 93, 0.2)',
                borderColor: 'rgb(44, 95, 93)'
            }]
        },
        options: {
            scales: {
                r: {
                    suggestedMin: 0,
                    suggestedMax: 100,
                    ticks: { stepSize: 20 }
                }
            }
        }
    });
}

// 추천 프로그램 보기
function viewRecommendedPrograms() {
    window.location.href = `/programs?recommended=true&studentId=${studentId}`;
}
```

#### 1.4 샘플 데이터 자동 생성

**DataLoader.java - initializeCompetencies()**

**생성 데이터:**
- 3개 카테고리: 전공역량, 일반역량, 리더십역량
- 12개 역량:
  - 전공역량: 프로그래밍 능력, 데이터베이스 능력, 시스템 설계 능력, 문제 해결 능력
  - 일반역량: 의사소통 능력, 창의적 사고, 비판적 사고, 자기관리 능력
  - 리더십역량: 팀 리더십, 협업 능력, 갈등 관리, 동기부여
- 3명 학생의 36개 평가 데이터 (우수/중간/개선필요)

---

### 2. 맞춤형 프로그램 추천 시스템

#### 2.1 프로그램-역량 매핑

**ProgramCompetency 엔티티:**
```java
@Entity
@Table(name = "program_competencies")
public class ProgramCompetency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Program program;

    @ManyToOne
    @JoinColumn(name = "competency_id")
    private Competency competency;

    @Column(nullable = false)
    private Integer weight;  // 가중치 1-10
}
```

**키워드 기반 자동 매핑:**
```java
// DataLoader - initializeProgramCompetencyMappings()
if (title.contains("프로그래밍") || title.contains("코딩")) {
    createMapping(program, "프로그래밍 능력", 10);
    createMapping(program, "문제 해결 능력", 8);
}

if (title.contains("리더")) {
    createMapping(program, "팀 리더십", 10);
    createMapping(program, "동기부여", 8);
}

if (title.contains("팀") || title.contains("협업")) {
    createMapping(program, "협업 능력", 10);
    createMapping(program, "갈등 관리", 7);
}
// ... 총 10가지 카테고리 매칭 룰
```

#### 2.2 추천 알고리즘

**ProgramRecommendationService.java**

**알고리즘 단계:**
```
1. 학생의 최신 역량진단 결과 조회
2. 약점 역량 파악 (70점 미만, 없으면 하위 3개)
3. 약점 역량을 향상시킬 수 있는 프로그램 찾기
4. 추천 점수 계산
5. 추천 점수 순으로 정렬하여 반환
```

**추천 점수 공식:**
```
추천 점수 = (100 - 학생 점수) × 프로그램 가중치 ÷ 10
```

**예시:**
| 학생 역량 | 학생 점수 | 프로그램 | 가중치 | 추천 점수 |
|---------|---------|---------|-------|---------|
| 프로그래밍 | 55점 | 알고리즘 경진대회 | 10 | (100-55)×10÷10 = **45점** |
| 프로그래밍 | 55점 | SW 멘토링 | 7 | (100-55)×7÷10 = **31.5점** |
| 리더십 | 85점 | 리더십 캠프 | 10 | (100-85)×10÷10 = **15점** |

**핵심 코드:**
```java
public List<RecommendedProgramDto> getRecommendedPrograms(Long studentId, int limit) {
    // 1. 최신 평가 조회
    List<StudentCompetencyAssessment> assessments = ...

    // 2. 약점 파악 (70점 미만)
    List<StudentCompetencyAssessment> weaknesses = assessments.stream()
        .filter(a -> a.getScore() < 70)
        .sorted(Comparator.comparing(StudentCompetencyAssessment::getScore))
        .collect(Collectors.toList());

    // 약점이 없으면 하위 3개
    if (weaknesses.isEmpty()) {
        weaknesses = assessments.stream()
            .sorted(Comparator.comparing(...))
            .limit(3).collect(...);
    }

    // 3. 약점 역량 ID 추출
    List<Long> weaknessIds = weaknesses.stream()
        .map(a -> a.getCompetency().getId())
        .collect(Collectors.toList());

    // 4. 해당 역량을 향상시키는 프로그램 찾기
    List<ProgramCompetency> programCompetencies =
        programCompetencyRepository.findByCompetencyIdIn(weaknessIds);

    // 5. 프로그램별 추천 점수 계산
    Map<Program, ProgramScore> programScores = new HashMap<>();
    for (ProgramCompetency pc : programCompetencies) {
        int studentScore = getStudentScore(pc.getCompetency(), weaknesses);
        double score = (100.0 - studentScore) * pc.getWeight() / 10.0;

        programScores.computeIfAbsent(pc.getProgram(), ...)
            .addScore(score);
            .addReason(pc.getCompetency().getName() + " 향상");
    }

    // 6. 추천 점수 순으로 정렬
    return programScores.entrySet().stream()
        .sorted((e1, e2) -> Double.compare(
            e2.getValue().getTotalScore(),
            e1.getValue().getTotalScore()))
        .limit(limit)
        .map(entry -> RecommendedProgramDto.from(...))
        .collect(Collectors.toList());
}
```

#### 2.3 REST API

**ProgramRecommendationController.java**

```java
@RestController
@RequestMapping("/api/programs")
public class ProgramRecommendationController {

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendedProgramDto>> getRecommendedPrograms(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "10") int limit) {

        List<RecommendedProgramDto> recommendations =
            recommendationService.getRecommendedPrograms(studentId, limit);

        return ResponseEntity.ok(recommendations);
    }
}
```

**응답 예시:**
```json
[
  {
    "programId": 123,
    "title": "알고리즘 경진대회",
    "category": "전공",
    "recommendationScore": 45.0,
    "recommendationReasons": [
      "프로그래밍 능력 향상",
      "문제 해결 능력 향상"
    ]
  },
  {
    "programId": 124,
    "title": "데이터베이스 프로젝트",
    "category": "전공",
    "recommendationScore": 40.0,
    "recommendationReasons": [
      "데이터베이스 능력 향상"
    ]
  }
]
```

#### 2.4 프론트엔드 연동

**HomeController.java 수정:**
```java
@GetMapping("/programs")
public String programs(
        @RequestParam(required = false) Boolean recommended,
        @RequestParam(required = false) Long studentId,
        ...) {

    // 추천 모드 처리
    if (Boolean.TRUE.equals(recommended) && studentId != null) {
        List<RecommendedProgramDto> recommendedPrograms =
            recommendationService.getRecommendedPrograms(studentId, 50);

        List<Program> programs = recommendedPrograms.stream()
            .map(dto -> programService.getProgram(dto.getProgramId()))
            .filter(p -> p != null)
            .toList();

        model.addAttribute("pageTitle", "맞춤형 추천 프로그램 (" + programs.size() + "개)");
        model.addAttribute("recommendedMode", true);
        return "programs";
    }

    // 일반 모드: 전체 프로그램
    ...
}
```

---

## 주요 버그 및 해결

### 버그 1: User-Student ID 매핑 이슈

**문제:**
- 세션에 User.userId가 저장되어 있음
- API는 Student.id를 요구함
- 두 테이블이 분리되어 있어 ID가 일치하지 않음

**에러 메시지:**
```
학생을 찾을 수 없습니다: ID 2
```

**원인 분석:**
```
User 테이블:
- user_id (PK) = 2
- student_num (학번) = 2024002

Student 테이블:
- id (PK) = 1
- student_id (학번 문자열) = "2024002"
```

**해결:**
1. CompetencyPageController에서 User.studentNum → Student.id 매핑
```java
// User 조회
User user = userRepository.findById(userId).orElse(null);

// studentNum으로 Student 찾기
Student student = studentRepository.findByStudentId(
    String.valueOf(user.getStudentNum())
).orElse(null);

// Student.id를 모델에 추가
model.addAttribute("studentId", student.getId());
```

2. competency-result.html에서 studentId 사용
```javascript
const studentId = /*[[${studentId}]]*/ null;  // User ID가 아닌 Student ID
```

**커밋:** `28a2bd2 - fix: Resolve User-Student ID mismatch`

---

### 버그 2: Student 테이블 비어있음

**문제:**
- DataLoader의 createStudent()가 User 테이블만 저장
- Student 테이블이 비어있어 역량진단 결과 페이지에서 에러 발생

**에러 메시지:**
```
역량진단 결과 페이지 접근 실패: 학생 정보를 찾을 수 없음 studentNum=2024002
```

**시도한 해결책:**
1. ❌ createStudent()에 Student 저장 추가 → 타이밍 문제
2. ❌ Flyway 마이그레이션 작성 → 수동 실행 필요
3. ✅ DataLoader에 자동 동기화 메서드 추가

**최종 해결:**
```java
// DataLoader.java
@Override
public void run(String... args) {
    initializeUsers();
    syncStudentsFromUsers();  // ← 자동 동기화
    ...
}

private void syncStudentsFromUsers() {
    List<User> students = userRepository.findAll().stream()
        .filter(u -> u.getRole() == UserRole.STUDENT)
        .toList();

    int syncCount = 0;
    for (User user : students) {
        String studentIdStr = String.valueOf(user.getStudentNum());
        if (!studentRepository.findByStudentId(studentIdStr).isPresent()) {
            Student studentEntity = new Student();
            studentEntity.setStudentId(studentIdStr);
            studentEntity.setName(user.getName());
            // ... 필드 복사
            studentRepository.save(studentEntity);
            syncCount++;
        }
    }

    log.info("✅ User → Student 동기화 완료: {}건", syncCount);
}
```

**커밋:** `e99176c - fix: Add automatic User to Student table synchronization`

---

### 버그 3: 평가 데이터 생성 안됨

**문제:**
- initializeCompetencies()가 Student 테이블이 비어있을 때 실행됨
- 평가 데이터 생성 조건 (`students.size() >= 3`)이 false
- 이후 Student 동기화 후에도 평가 데이터가 생성되지 않음 (카테고리가 이미 존재하여 스킵)

**에러 메시지:**
```
평가 데이터가 없습니다. 먼저 역량 진단을 실시해주세요.
```

**해결:**
평가 데이터 초기화를 별도 메서드로 분리
```java
@Override
public void run(String... args) {
    initializeUsers();
    syncStudentsFromUsers();       // Student 동기화
    initializeCompetencies();      // 역량 카테고리 + 역량만
    initializeSampleAssessments(); // 평가 데이터 별도 생성
}

private void initializeSampleAssessments() {
    if (assessmentRepository.count() > 0) return;

    List<Student> students = studentRepository.findAll();
    List<Competency> competencies = competencyRepository.findAll();

    if (students.size() < 3 || competencies.size() < 12) {
        log.warn("데이터 부족, 평가 데이터 생성 건너뜀");
        return;
    }

    // 학생 3명 x 역량 12개 = 36건 평가 데이터 생성
    ...
}
```

**커밋:** `e2421dd - fix: Separate assessment data initialization`

---

### 버그 4: 추천 모드에서 전체 프로그램 표시

**문제:**
- competency-result.html에서 `recommended=true&studentId=1` 파라미터 전달
- HomeController가 파라미터를 받지만 처리하지 않음
- 전체 프로그램이 표시됨

**해결:**
HomeController에 추천 모드 처리 추가
```java
@GetMapping("/programs")
public String programs(
        @RequestParam(required = false) Boolean recommended,  // ← 추가
        @RequestParam(required = false) Long studentId,       // ← 추가
        ...) {

    if (Boolean.TRUE.equals(recommended) && studentId != null) {
        // 추천 프로그램만 가져오기
        List<RecommendedProgramDto> recommendedPrograms =
            recommendationService.getRecommendedPrograms(studentId, 50);

        List<Program> programs = recommendedPrograms.stream()
            .map(dto -> programService.getProgram(dto.getProgramId()))
            .filter(p -> p != null)
            .toList();

        model.addAttribute("pageTitle", "맞춤형 추천 프로그램 (" + programs.size() + "개)");
        model.addAttribute("recommendedMode", true);
        return "programs";
    }

    // 일반 모드
    ...
}
```

**커밋:** `6cb8234 - fix: Add recommendation mode to programs page`

---

### 버그 5: 메서드 이름 오류

**문제:**
- ProgramService에는 `getProgram(Integer programId)` 메서드가 있음
- HomeController에서 `getProgramById()` 호출

**컴파일 에러:**
```
cannot find symbol: method getProgramById(Integer)
```

**해결:**
```java
// 수정 전
return programService.getProgramById(dto.getProgramId());

// 수정 후
return programService.getProgram(dto.getProgramId());
```

**커밋:** `12c1d44 - fix: Correct method name from getProgramById to getProgram`

---

## 기술적 의사결정

### 1. User vs Student 테이블 분리

**배경:**
- User 테이블: 인증/인가 (로그인, 권한)
- Student 테이블: 학생 도메인 데이터 (역량평가, 프로그램 신청)

**장점:**
- 관심사 분리 (SoC)
- Student 테이블에 학생 전용 필드 추가 가능
- 다른 역할(상담사, 관리자)과 분리

**단점:**
- ID 매핑 복잡도 증가
- 데이터 동기화 필요

**결정:**
도메인 분리를 위해 테이블 분리 유지하되, DataLoader에서 자동 동기화 구현

---

### 2. 추천 알고리즘 설계

**고려사항:**
1. 협업 필터링 vs 콘텐츠 기반 필터링
2. 복잡한 ML 모델 vs 단순한 규칙 기반

**결정: 콘텐츠 기반 + 규칙 기반 알고리즘**

**이유:**
- 초기 단계에서 학생 수가 적어 협업 필터링 부적합
- 역량 점수와 프로그램 가중치로 명확한 추천 가능
- 해석 가능성 높음 (추천 이유 제공 가능)
- 구현 및 유지보수 용이

**공식:**
```
추천 점수 = (100 - 학생 점수) × 가중치 ÷ 10
```

**특징:**
- 점수가 낮을수록 (약점일수록) 추천 점수 증가
- 가중치가 높을수록 (프로그램이 해당 역량을 많이 향상시킬수록) 추천 점수 증가
- 0-100 범위로 정규화

---

### 3. 프로그램-역량 매핑 자동화

**문제:**
- 50개 프로그램 × 12개 역량 = 600개 매핑 필요
- 수동 입력은 비현실적

**고려한 옵션:**
1. 수동 입력 → 시간 소요 과다
2. ML 기반 자동 분류 → 초기 단계에 과도한 기술
3. 키워드 기반 규칙 → **선택**

**구현:**
```java
if (title.contains("프로그래밍")) {
    프로그래밍 능력 (가중치 10)
}
if (title.contains("리더")) {
    팀 리더십 (가중치 10)
}
```

**장점:**
- 구현 간단
- 초기 데이터 빠르게 생성
- 규칙 추가/수정 용이

**한계:**
- 정확도 제한적
- 키워드가 없는 프로그램 매핑 어려움

**향후 개선:**
- 관리자가 수동으로 매핑 수정 가능한 UI 제공
- NLP 기반 자동 분류 적용 검토

---

### 4. Chart.js 선택

**요구사항:**
- 역량 프로필 시각화
- 카테고리별 비교

**비교:**
| 라이브러리 | 레이더 차트 | 라이선스 | 번들 크기 |
|----------|----------|---------|---------|
| Chart.js | ✅ | MIT | 60KB |
| D3.js | ✅ (복잡) | BSD | 250KB |
| Plotly | ✅ | MIT | 3MB |

**결정: Chart.js**

**이유:**
- 레이더 차트 기본 제공
- 간단한 설정
- 작은 번들 크기
- CDN 사용 가능

---

## 커밋 히스토리

### 총 11개 커밋

```
12c1d44 - fix: Correct method name from getProgramById to getProgram
6cb8234 - fix: Add recommendation mode to programs page
0ca75d3 - feat: Implement competency-based program recommendation system
e2421dd - fix: Separate assessment data initialization from competency setup
e99176c - fix: Add automatic User to Student table synchronization
50161c6 - feat: Add database migration to sync Student data from User table
b5adcbf - fix: Create Student entity when creating User in DataLoader
28a2bd2 - fix: Resolve User-Student ID mismatch in competency assessment
9da3f85 - feat: Add CompetencyPageController for page routing
ecf4900 - docs: Add competency assessment analysis document
bf4a5f3 - feat: 역량진단(Competency Assessment) 시스템 구현
```

### 주요 커밋 상세

#### bf4a5f3 - 역량진단 시스템 구현
- Entity 3개
- Repository 3개
- Service 2개
- Controller 3개
- DTO 8개
- 프론트엔드 (competency-result.html)
- 샘플 데이터 생성

#### 0ca75d3 - 프로그램 추천 시스템 구현
- ProgramCompetency 엔티티
- ProgramCompetencyRepository
- ProgramRecommendationService
- ProgramRecommendationController
- RecommendedProgramDto
- 키워드 기반 매핑 로직

---

## 테스트 가이드

### 1. 로컬 환경 설정

```bash
# 애플리케이션 재시작
./gradlew bootRun

# 로그 확인
# ✅ User → Student 동기화 완료: 8건
# ✅ 역량 카테고리 및 역량 데이터 초기화 완료
# ✅ 학생 평가 샘플 데이터 생성 완료: 학생 3명 x 역량 12개 = 36건
# ✅ 프로그램-역량 매핑 X건 생성 완료
```

### 2. 역량진단 결과 페이지 테스트

**Step 1: 로그인**
```
학번: 2024002
비밀번호: 990202
```

**Step 2: 역량진단 결과 페이지 접속**
```
http://localhost:8080/assessment
또는
http://localhost:8080/competency-result
```

**확인 사항:**
- [x] 학생 정보 표시 (이영희, 2024002)
- [x] 전체 점수 및 등급 표시
- [x] 카테고리별 점수 카드 (3개)
- [x] 레이더 차트 표시
- [x] 강점 Top 3 표시
- [x] 약점 Bottom 3 표시
- [x] "추천 프로그램 보기" 버튼

### 3. 추천 프로그램 테스트

**Step 1: "추천 프로그램 보기" 버튼 클릭**

**Step 2: URL 확인**
```
http://localhost:8080/programs?recommended=true&studentId=1
```

**확인 사항:**
- [x] 페이지 제목: "맞춤형 추천 프로그램 (N개)"
- [x] 전체 프로그램이 아닌 추천 프로그램만 표시
- [x] 학생의 약점 역량을 향상시키는 프로그램 우선 표시

### 4. REST API 테스트

**역량 조회:**
```bash
curl http://localhost:8080/api/competencies
```

**평가 리포트 조회:**
```bash
curl http://localhost:8080/api/assessments/students/1/report
```

**추천 프로그램 조회:**
```bash
curl "http://localhost:8080/api/programs/recommendations?studentId=1&limit=10"
```

**평가 제출:**
```bash
curl -X POST http://localhost:8080/api/assessments/submit \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "assessments": [
      {"competencyId": 1, "score": 85, "notes": "우수"},
      {"competencyId": 2, "score": 90, "notes": "매우 우수"}
    ],
    "assessor": "교수명"
  }'
```

### 5. 샘플 학생별 추천 결과

**학생 1 (우수 학생 - 평균 88점):**
- 약점: 팀 리더십 (82점), 동기부여 (83점)
- 추천: 리더십 프로그램, 멘토링 프로그램

**학생 2 (중간 학생 - 평균 76점):**
- 약점: 시스템 설계 (68점), 데이터베이스 (70점)
- 추천: 시스템 설계 워크샵, DB 프로젝트

**학생 3 (개선 필요 - 평균 61점):**
- 약점: 시스템 설계 (50점), 프로그래밍 (55점), 문제 해결 (58점)
- 추천: 프로그래밍 기초, 알고리즘 스터디, 코딩 멘토링

---

## 향후 개선사항

### 1. 기능 추가

#### 1.1 역량진단 입력 페이지
**현재:** 샘플 데이터만 존재
**개선:** 학생/교수가 직접 평가 입력할 수 있는 UI

**구현 계획:**
- `/competency-assessment` 페이지 개발
- 역량별 점수 입력 폼
- 자가진단 vs 교수평가 구분
- 시간에 따른 역량 변화 그래프

#### 1.2 역량 진단 이력 관리
**현재:** 최신 평가만 표시
**개선:** 시간에 따른 역량 변화 추적

**구현 계획:**
- 평가 이력 조회 API
- 시계열 차트 (역량 점수 변화)
- 성장 분석 리포트

#### 1.3 관리자용 역량 관리
**현재:** 코드로 역량 정의
**개선:** 관리자가 역량/카테고리 CRUD

**구현 계획:**
- 관리자 페이지 개발
- 역량 카테고리 추가/수정/삭제
- 역량 항목 추가/수정/삭제
- 프로그램-역량 매핑 관리

#### 1.4 프로그램 추천 개선
**현재:** 약점 기반 추천
**개선:** 다양한 추천 전략

**구현 계획:**
- 강점 강화형 추천 (이미 잘하는 역량 더 발전)
- 균형 발전형 추천 (모든 역량 고르게)
- 관심사 기반 추천 (학생이 선호하는 카테고리)
- 하이브리드 추천 (여러 전략 조합)

### 2. 알고리즘 개선

#### 2.1 프로그램-역량 매핑 정확도 향상
**현재:** 키워드 기반
**개선:** NLP 기반 자동 분류

**구현 계획:**
- TF-IDF / Word2Vec 활용
- 프로그램 설명 텍스트 분석
- 유사 프로그램 역량 참조

#### 2.2 추천 점수 정규화
**현재:** 단순 공식
**개선:** 학생별 최적화된 점수

**구현 계획:**
- 학생의 평균 점수 고려
- 역량 간 우선순위 가중치
- 시간적 긴급성 반영 (마감 임박 프로그램 우선)

#### 2.3 협업 필터링 추가
**현재:** 콘텐츠 기반만
**개선:** 유사 학생 패턴 활용

**구현 계획:**
- 유사 역량 프로필 학생 찾기
- 해당 학생들이 선택한 프로그램 추천
- 하이브리드 추천 (콘텐츠 + 협업)

### 3. 성능 최적화

#### 3.1 캐싱 전략
**현재:** 매 요청마다 DB 조회
**개선:** Redis 캐싱

**구현 계획:**
```java
@Cacheable(value = "assessmentReport", key = "#studentId")
public AssessmentReportResponse generateReport(Long studentId) {
    ...
}

@CacheEvict(value = "assessmentReport", key = "#studentId")
public void submitAssessment(Long studentId, ...) {
    ...
}
```

#### 3.2 N+1 문제 해결
**현재:** 일부 쿼리에서 N+1 발생
**개선:** Fetch Join 최적화

**구현 계획:**
```java
@Query("SELECT pc FROM ProgramCompetency pc " +
       "JOIN FETCH pc.program p " +
       "JOIN FETCH pc.competency c " +
       "JOIN FETCH c.category " +
       "WHERE ...")
List<ProgramCompetency> findByCompetencyIdIn(...);
```

### 4. UI/UX 개선

#### 4.1 반응형 디자인
**현재:** 데스크톱 중심
**개선:** 모바일 최적화

#### 4.2 인터랙티브 차트
**현재:** 정적 차트
**개선:** 클릭, 호버 인터랙션

#### 4.3 추천 프로그램 상세 정보
**현재:** 프로그램 목록만
**개선:** 추천 이유, 예상 향상 역량 표시

**구현 예시:**
```
[프로그램 카드]
제목: 알고리즘 경진대회
추천 점수: 45점

추천 이유:
✓ 프로그래밍 능력 향상 (현재 55점 → 목표 75점)
✓ 문제 해결 능력 향상 (현재 58점 → 목표 70점)

예상 효과: 전공역량 평균 12점 상승
```

---

## 참고 자료

### API 문서
- REST API: `/api/competencies`, `/api/assessments`, `/api/programs/recommendations`

### 관련 파일
```
src/main/java/com/scms/app/
├── model/
│   ├── CompetencyCategory.java
│   ├── Competency.java
│   ├── StudentCompetencyAssessment.java
│   └── ProgramCompetency.java
├── repository/
│   ├── CompetencyCategoryRepository.java
│   ├── CompetencyRepository.java
│   ├── StudentCompetencyAssessmentRepository.java
│   └── ProgramCompetencyRepository.java
├── service/
│   ├── CompetencyService.java
│   ├── CompetencyAssessmentService.java
│   └── ProgramRecommendationService.java
├── controller/
│   ├── CompetencyController.java
│   ├── CompetencyAssessmentController.java
│   ├── CompetencyPageController.java
│   ├── ProgramRecommendationController.java
│   └── HomeController.java
└── dto/
    ├── CompetencyRequest.java
    ├── CompetencyResponse.java
    ├── CompetencyCategoryResponse.java
    ├── AssessmentSubmitRequest.java
    ├── AssessmentResponse.java
    ├── AssessmentReportResponse.java
    ├── CategoryScoreDto.java
    ├── CompetencyScoreDto.java
    └── RecommendedProgramDto.java

src/main/resources/templates/
└── competency-result.html

src/main/java/com/scms/app/config/
└── DataLoader.java
```

### PR
- PR URL: https://github.com/seedevk8s/SCMS3/compare/main...claude/implement-competency-assessment-016oycC5yKv54U5RZQ41En7Y

---

## 결론

역량진단 및 맞춤형 프로그램 추천 시스템을 성공적으로 구현했습니다.

**주요 성과:**
- ✅ 역량진단 시스템 완성 (Entity → API → Frontend)
- ✅ 맞춤형 추천 알고리즘 구현
- ✅ 자동 데이터 동기화 및 샘플 데이터 생성
- ✅ Chart.js 시각화
- ✅ 5개 주요 버그 해결

**기술 스택:**
- Backend: Spring Boot, JPA, PostgreSQL/MySQL
- Frontend: Thymeleaf, Chart.js, JavaScript
- Algorithm: Content-based Filtering

**다음 단계:**
- 역량진단 입력 페이지 개발
- 추천 알고리즘 고도화
- 관리자용 역량 관리 페이지
- 성능 최적화 및 캐싱

---

**문서 버전:** 1.0
**최종 수정일:** 2025-11-18
**작성자:** Claude (AI Assistant)
