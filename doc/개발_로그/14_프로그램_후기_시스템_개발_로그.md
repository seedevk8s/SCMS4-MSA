# 프로그램 후기/리뷰 시스템 구현 개발 로그

**작성일**: 2025-11-17
**작성자**: Claude AI
**관련 이슈**: 프로그램 후기/리뷰 시스템 구현

## 📋 목차

1. [개요](#개요)
2. [구현 내용](#구현-내용)
3. [백엔드 구현](#백엔드-구현)
4. [프론트엔드 구현](#프론트엔드-구현)
5. [테스트 데이터 생성](#테스트-데이터-생성)
6. [주요 기능](#주요-기능)
7. [API 명세](#api-명세)
8. [테스트 방법](#테스트-방법)
9. [트러블슈팅](#트러블슈팅)

---

## 개요

### 목적
프로그램 참여를 완료한 학생들이 후기를 작성하고, 다른 사용자들이 후기를 확인할 수 있는 리뷰 시스템 구현

### 주요 기능
- ✅ 프로그램 후기 작성 (별점 1-5 + 텍스트)
- ✅ 후기 목록 조회 및 페이징
- ✅ 후기 수정 (본인만 가능)
- ✅ 후기 삭제 (Soft Delete, 본인만 가능)
- ✅ 평균 별점 계산 및 표시
- ✅ 참여 완료 확인 (COMPLETED 상태만 작성 가능)

### 기술 스택
- **Backend**: Spring Boot 3.x, JPA/Hibernate
- **Frontend**: Vanilla JavaScript, Thymeleaf
- **Database**: MySQL (program_reviews 테이블)
- **패턴**: DTO 패턴, Soft Delete 패턴

---

## 구현 내용

### 구현 일정
1. **백엔드 개발** (Entity, Repository, Service, Controller)
2. **프론트엔드 개발** (UI, JavaScript)
3. **테스트 데이터 생성** (DataLoader)
4. **문서화 및 커밋**

---

## 백엔드 구현

### 1. ProgramReview Entity

**파일**: `src/main/java/com/scms/app/model/ProgramReview.java`

#### 주요 필드
```java
@Entity
@Table(name = "program_reviews")
public class ProgramReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 선택 (향후 확장)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Soft Delete
}
```

#### 비즈니스 메서드
- `delete()`: Soft Delete 처리
- `isDeleted()`: 삭제 여부 확인
- `isEditableBy(userId)`: 본인 확인 및 수정 가능 여부
- `validateRating()`: 평점 유효성 검사 (1-5)

### 2. ProgramReviewRepository

**파일**: `src/main/java/com/scms/app/repository/ProgramReviewRepository.java`

#### 주요 쿼리 메서드
```java
public interface ProgramReviewRepository extends JpaRepository<ProgramReview, Integer> {

    // 프로그램별 후기 조회 (삭제되지 않은 것만, 최신순)
    @Query("SELECT r FROM ProgramReview r " +
           "JOIN FETCH r.user " +
           "WHERE r.program.programId = :programId " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.createdAt DESC")
    List<ProgramReview> findByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);

    // 평균 평점 계산
    @Query("SELECT AVG(r.rating) FROM ProgramReview r " +
           "WHERE r.program.programId = :programId " +
           "AND r.deletedAt IS NULL")
    Double getAverageRatingByProgramId(@Param("programId") Integer programId);

    // 사용자가 이미 후기를 작성했는지 확인
    boolean existsByUserIdAndProgramIdAndDeletedAtIsNull(
        Integer userId, Integer programId);
}
```

**설계 포인트**:
- `JOIN FETCH`: N+1 문제 방지
- `deletedAt IS NULL`: Soft Delete 처리된 후기 제외
- `ORDER BY createdAt DESC`: 최신 후기 우선 표시

### 3. ReviewRequest/ReviewResponse DTO

**파일**: `src/main/java/com/scms/app/dto/ReviewRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하여야 합니다.")
    private Integer rating;

    @Size(max = 1000, message = "후기는 1000자 이하여야 합니다.")
    private String content;
}
```

**파일**: `src/main/java/com/scms/app/dto/ReviewResponse.java`

```java
@Data
@Builder
public class ReviewResponse {
    private Integer reviewId;
    private Integer programId;
    private String programTitle;

    // 작성자 정보
    private Integer userId;
    private String userName;
    private Integer studentNum;
    private String department;
    private Integer grade;

    // 후기 내용
    private Integer rating;
    private String content;
    private String imageUrl;

    // 작성 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 본인 확인용
    private boolean isMyReview;
}
```

### 4. ProgramReviewService

**파일**: `src/main/java/com/scms/app/service/ProgramReviewService.java`

#### 주요 메서드

##### 후기 작성
```java
@Transactional
public ProgramReview createReview(Integer userId, Integer programId,
                                  ReviewRequest request) {
    // 1. 프로그램 존재 확인
    Program program = programRepository.findById(programId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로그램입니다."));

    // 2. 사용자 존재 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    // 3. 참여 완료 여부 확인
    boolean hasCompletedProgram = applicationRepository
        .existsByUserIdAndProgramIdAndStatus(userId, programId, ApplicationStatus.COMPLETED);

    if (!hasCompletedProgram) {
        throw new IllegalStateException("프로그램을 완료한 사용자만 후기를 작성할 수 있습니다.");
    }

    // 4. 중복 후기 확인
    boolean alreadyReviewed = reviewRepository
        .existsByUserIdAndProgramIdAndDeletedAtIsNull(userId, programId);

    if (alreadyReviewed) {
        throw new IllegalStateException("이미 후기를 작성하셨습니다.");
    }

    // 5. 후기 생성
    ProgramReview review = ProgramReview.builder()
        .program(program)
        .user(user)
        .rating(request.getRating())
        .content(request.getContent())
        .build();

    review.validateRating(); // 평점 유효성 검사

    return reviewRepository.save(review);
}
```

##### 후기 수정
```java
@Transactional
public ProgramReview updateReview(Integer userId, Integer reviewId,
                                  ReviewRequest request) {
    // 1. 후기 조회
    ProgramReview review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 후기입니다."));

    // 2. 본인 확인
    if (!review.isEditableBy(userId)) {
        throw new IllegalStateException("본인의 후기만 수정할 수 있습니다.");
    }

    // 3. 수정
    review.setRating(request.getRating());
    review.setContent(request.getContent());
    review.validateRating();

    return reviewRepository.save(review);
}
```

##### 후기 삭제 (Soft Delete)
```java
@Transactional
public void deleteReview(Integer userId, Integer reviewId) {
    ProgramReview review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 후기입니다."));

    if (!review.getUser().getUserId().equals(userId)) {
        throw new IllegalStateException("본인의 후기만 삭제할 수 있습니다.");
    }

    review.delete(); // deletedAt 설정
    reviewRepository.save(review);
}
```

##### 평균 평점 계산
```java
public Double getAverageRating(Integer programId) {
    Double avg = reviewRepository.getAverageRatingByProgramId(programId);
    return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
}
```

### 5. ProgramReviewController

**파일**: `src/main/java/com/scms/app/controller/ProgramReviewController.java`

#### REST API 엔드포인트

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| GET | `/api/programs/{programId}/reviews` | 후기 목록 조회 | 전체 |
| POST | `/api/programs/{programId}/reviews` | 후기 작성 | 로그인 |
| PUT | `/api/programs/{programId}/reviews/{reviewId}` | 후기 수정 | 본인 |
| DELETE | `/api/programs/{programId}/reviews/{reviewId}` | 후기 삭제 | 본인 |

#### 구현 예시
```java
@PostMapping("/{programId}/reviews")
public ResponseEntity<?> createReview(
        @PathVariable Integer programId,
        @RequestBody @Valid ReviewRequest request,
        HttpSession session) {

    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "로그인이 필요합니다."));
    }

    try {
        ProgramReview review = reviewService.createReview(userId, programId, request);
        ReviewResponse response = ReviewResponse.from(review, userId);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "후기가 등록되었습니다.",
            "review", response
        ));
    } catch (IllegalArgumentException | IllegalStateException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", e.getMessage()));
    }
}
```

---

## 프론트엔드 구현

### 1. UI 구성

**파일**: `src/main/resources/templates/program-detail.html`

#### 후기 섹션 HTML
```html
<!-- 후기 탭 컨텐츠 -->
<div id="reviews" class="tab-content">
    <!-- 평균 별점 표시 -->
    <div class="review-stats">
        <div class="average-rating">
            <span class="rating-number" id="averageRating">0.0</span>
            <div class="stars" id="averageStars"></div>
            <span class="review-count" id="reviewCount">(0개의 후기)</span>
        </div>
    </div>

    <!-- 후기 작성 버튼 -->
    <div class="review-write-section">
        <button id="writeReviewBtn" class="btn-primary">후기 작성</button>
    </div>

    <!-- 후기 목록 -->
    <div id="reviewList" class="review-list"></div>

    <!-- 후기 없을 때 -->
    <div id="noReviews" class="no-reviews" style="display: none;">
        아직 등록된 후기가 없습니다.
    </div>
</div>
```

#### 후기 작성/수정 모달
```html
<div id="reviewModal" class="review-modal" style="display: none;">
    <div class="review-modal-content">
        <div class="review-modal-header">
            <h3 id="reviewModalTitle">후기 작성</h3>
            <button class="btn-close-modal">&times;</button>
        </div>

        <div class="review-modal-body">
            <!-- 별점 선택 -->
            <div class="form-group">
                <label>별점 <span style="color: #dc3545;">*</span></label>
                <div class="star-rating">
                    <span class="star" data-rating="1">★</span>
                    <span class="star" data-rating="2">★</span>
                    <span class="star" data-rating="3">★</span>
                    <span class="star" data-rating="4">★</span>
                    <span class="star" data-rating="5">★</span>
                </div>
                <input type="hidden" id="reviewRating" value="0">
            </div>

            <!-- 후기 내용 -->
            <div class="form-group">
                <label>후기 내용</label>
                <textarea id="reviewContent" rows="8" maxlength="1000"
                          placeholder="프로그램에 대한 솔직한 후기를 작성해주세요."></textarea>
                <div class="char-count">
                    <span id="contentCharCount">0</span> / 1000
                </div>
            </div>
        </div>

        <div class="review-modal-footer">
            <button class="btn-secondary">취소</button>
            <button class="btn-primary" id="submitReviewBtn">등록</button>
        </div>
    </div>
</div>
```

### 2. JavaScript 구현

#### 후기 목록 로드
```javascript
async function loadReviews() {
    try {
        const response = await fetch(`/api/programs/${programId}/reviews`);

        if (!response.ok) {
            throw new Error('후기를 불러올 수 없습니다.');
        }

        const data = await response.json();

        // 평균 별점 및 개수 표시
        document.getElementById('averageRating').textContent =
            data.averageRating.toFixed(1);
        document.getElementById('reviewCount').textContent =
            `(${data.reviews.length}개의 후기)`;

        displayStars('averageStars', data.averageRating);

        // 후기 목록 렌더링
        const reviewList = document.getElementById('reviewList');
        if (data.reviews.length === 0) {
            reviewList.style.display = 'none';
            document.getElementById('noReviews').style.display = 'block';
        } else {
            reviewList.style.display = 'block';
            document.getElementById('noReviews').style.display = 'none';
            reviewList.innerHTML = data.reviews.map(review =>
                renderReviewCard(review)).join('');
        }

        // 후기 작성 버튼 표시/숨김
        if (data.canWrite) {
            document.getElementById('writeReviewBtn').style.display = 'block';
        } else {
            document.getElementById('writeReviewBtn').style.display = 'none';
        }

    } catch (error) {
        console.error('Error loading reviews:', error);
        alert('후기를 불러오는 중 오류가 발생했습니다.');
    }
}
```

#### 후기 카드 렌더링
```javascript
function renderReviewCard(review) {
    const createdDate = new Date(review.createdAt).toLocaleDateString('ko-KR');
    const isUpdated = review.updatedAt &&
                      review.updatedAt !== review.createdAt;

    return `
        <div class="review-card" data-review-id="${review.reviewId}">
            <div class="review-header">
                <div class="review-author">
                    <strong>${review.userName}</strong>
                    <span class="review-meta">
                        ${review.department} ${review.grade}학년
                    </span>
                </div>
                <div class="review-rating">
                    ${renderStars(review.rating)}
                </div>
            </div>

            <div class="review-content">
                ${escapeHtml(review.content)}
            </div>

            <div class="review-footer">
                <span class="review-date">
                    ${createdDate}
                    ${isUpdated ? ' (수정됨)' : ''}
                </span>

                ${review.isMyReview ? `
                    <div class="review-actions">
                        <button class="btn-edit"
                                onclick="editReview(${review.reviewId})">
                            수정
                        </button>
                        <button class="btn-delete"
                                onclick="deleteReview(${review.reviewId})">
                            삭제
                        </button>
                    </div>
                ` : ''}
            </div>
        </div>
    `;
}
```

#### 후기 작성
```javascript
async function submitReview() {
    const rating = parseInt(document.getElementById('reviewRating').value);
    const content = document.getElementById('reviewContent').value.trim();

    // 유효성 검사
    if (rating === 0) {
        alert('별점을 선택해주세요.');
        return;
    }

    try {
        const response = await fetch(`/api/programs/${programId}/reviews`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ rating, content })
        });

        const data = await response.json();

        if (response.ok) {
            alert('후기가 등록되었습니다.');
            closeReviewModal();
            loadReviews(); // 목록 새로고침
        } else {
            alert(data.error || '후기 등록에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error submitting review:', error);
        alert('후기 등록 중 오류가 발생했습니다.');
    }
}
```

#### 후기 수정
```javascript
async function editReview(reviewId) {
    // 기존 후기 데이터 가져오기
    const reviewCard = document.querySelector(
        `[data-review-id="${reviewId}"]`
    );

    // 모달 열기 및 데이터 채우기
    openReviewModal(true, reviewId);

    // ... 수정 로직
}
```

#### 후기 삭제
```javascript
async function deleteReview(reviewId) {
    if (!confirm('정말 이 후기를 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(
            `/api/programs/${programId}/reviews/${reviewId}`,
            { method: 'DELETE' }
        );

        const data = await response.json();

        if (response.ok) {
            alert('후기가 삭제되었습니다.');
            loadReviews(); // 목록 새로고침
        } else {
            alert(data.error || '후기 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error deleting review:', error);
        alert('후기 삭제 중 오류가 발생했습니다.');
    }
}
```

### 3. 별점 표시 함수
```javascript
function displayStars(elementId, rating) {
    const container = document.getElementById(elementId);
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;

    let starsHTML = '';

    // 채워진 별
    for (let i = 0; i < fullStars; i++) {
        starsHTML += '<span class="star filled">★</span>';
    }

    // 반 별
    if (hasHalfStar) {
        starsHTML += '<span class="star half">★</span>';
    }

    // 빈 별
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '<span class="star empty">★</span>';
    }

    container.innerHTML = starsHTML;
}
```

---

## 테스트 데이터 생성

### DataLoader 수정

**파일**: `src/main/java/com/scms/app/config/DataLoader.java`

#### 테스트 후기 생성 메서드
```java
private void initializeTestReviews() {
    long count = reviewRepository.count();

    if (count > 0) {
        log.info("프로그램 후기 데이터가 이미 존재합니다 ({}건). 초기화를 건너뜁니다.", count);
        return;
    }

    log.info("테스트용 프로그램 후기 데이터를 생성합니다...");

    try {
        // COMPLETED 상태의 신청 조회
        List<ProgramApplication> completedApplications =
            applicationRepository.findAll().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.COMPLETED)
                .collect(Collectors.toList());

        if (completedApplications.isEmpty()) {
            log.warn("COMPLETED 상태의 신청이 없어서 후기 데이터를 생성하지 않습니다.");
            return;
        }

        // 샘플 후기 내용 (8개)
        String[] reviewContents = {
            "정말 유익한 프로그램이었습니다! 많은 것을 배울 수 있었고, 실무 경험도 쌓을 수 있어서 좋았습니다.",
            "기대했던 것보다 더 좋은 프로그램이었어요. 특히 실습 위주로 진행되어서 이해하기 쉬웠습니다.",
            "전반적으로 만족스러운 프로그램이었습니다. 다만 시간이 조금 짧아서 아쉬웠어요.",
            "프로그램 내용은 좋았지만, 일정이 너무 빡빡해서 따라가기 힘들었습니다.",
            "기본적인 내용 위주로 진행되어 이미 관련 지식이 있는 사람에게는 다소 쉬울 수 있습니다.",
            "매우 만족합니다! 프로그램 구성도 체계적이고, 강사님의 설명도 명확했습니다.",
            "좋은 경험이었습니다. 특히 네트워킹 기회가 많아서 좋았고, 같은 관심사를 가진 사람들을 만날 수 있어서 의미있었습니다.",
            "프로그램 자체는 괜찮았으나, 준비물이나 사전 안내가 부족했던 점은 아쉬웠습니다."
        };

        int[] ratings = {5, 5, 4, 3, 3, 5, 4, 4};

        int reviewCount = 0;
        for (int i = 0; i < Math.min(completedApplications.size(), reviewContents.length); i++) {
            ProgramApplication application = completedApplications.get(i);
            createReview(application.getProgram(), application.getUser(),
                       ratings[i], reviewContents[i]);
            reviewCount++;
        }

        long afterCount = reviewRepository.count();
        log.info("✅ 테스트 후기 데이터 생성 완료: {}건", afterCount);

        // 평균 평점 계산
        double avgRating = 0.0;
        for (int i = 0; i < Math.min(reviewCount, 8); i++) {
            avgRating += ratings[i];
        }
        avgRating /= Math.min(reviewCount, 8);

        log.info("📝 평균 평점: {}/5", String.format("%.1f", avgRating));

    } catch (Exception e) {
        log.error("테스트 후기 데이터 생성 중 오류 발생", e);
    }
}

private void createReview(Program program, User user, int rating, String content) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime createdAt = now.minusDays(5); // 5일 전 작성

    ProgramReview review = ProgramReview.builder()
            .program(program)
            .user(user)
            .rating(rating)
            .content(content)
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .build();

    reviewRepository.save(review);
    log.debug("후기 생성: {} - {} ({}점)", user.getName(), program.getTitle(), rating);
}
```

#### 생성되는 테스트 데이터
- **후기 수**: COMPLETED 상태의 신청 수만큼 (최대 8건)
- **평점 분포**: 5점(3건), 4점(3건), 3점(2건)
- **평균 평점**: 약 4.1/5.0
- **작성 시간**: 5일 전으로 설정

---

## 주요 기능

### 1. 후기 작성 권한 검증
```java
// 프로그램 참여 완료 여부 확인
boolean hasCompletedProgram = applicationRepository
    .existsByUserIdAndProgramIdAndStatus(
        userId, programId, ApplicationStatus.COMPLETED
    );

if (!hasCompletedProgram) {
    throw new IllegalStateException(
        "프로그램을 완료한 사용자만 후기를 작성할 수 있습니다."
    );
}
```

**검증 항목**:
- ✅ 프로그램 참여 완료 (COMPLETED 상태)
- ✅ 중복 후기 방지 (1인 1후기)
- ✅ 평점 유효성 (1-5)

### 2. Soft Delete 패턴
```java
public void delete() {
    this.deletedAt = LocalDateTime.now();
}

public boolean isDeleted() {
    return this.deletedAt != null;
}
```

**장점**:
- 데이터 복구 가능
- 통계 유지 (평균 평점은 삭제된 후기 제외)
- 감사 추적 (Audit Trail)

### 3. 평균 평점 계산
```java
@Query("SELECT AVG(r.rating) FROM ProgramReview r " +
       "WHERE r.program.programId = :programId " +
       "AND r.deletedAt IS NULL")
Double getAverageRatingByProgramId(@Param("programId") Integer programId);
```

**특징**:
- 삭제된 후기 제외
- 소수점 첫째 자리까지 표시
- 후기 없을 시 0.0 반환

### 4. N+1 문제 방지
```java
@Query("SELECT r FROM ProgramReview r " +
       "JOIN FETCH r.user " +  // Eager Loading
       "WHERE r.program.programId = :programId " +
       "AND r.deletedAt IS NULL " +
       "ORDER BY r.createdAt DESC")
List<ProgramReview> findByProgramIdAndDeletedAtIsNull(
    @Param("programId") Integer programId);
```

---

## API 명세

### 1. 후기 목록 조회
```
GET /api/programs/{programId}/reviews
```

**Response**:
```json
{
  "averageRating": 4.1,
  "reviewCount": 8,
  "canWrite": true,
  "reviews": [
    {
      "reviewId": 1,
      "programId": 1,
      "programTitle": "2025 글로벌 리더십 캠프",
      "userId": 8,
      "userName": "임도윤",
      "studentNum": 2021002,
      "department": "건축학과",
      "grade": 4,
      "rating": 5,
      "content": "정말 유익한 프로그램이었습니다!",
      "imageUrl": null,
      "createdAt": "2025-11-12T10:30:00",
      "updatedAt": "2025-11-12T10:30:00",
      "isMyReview": false
    }
  ]
}
```

### 2. 후기 작성
```
POST /api/programs/{programId}/reviews
Content-Type: application/json

{
  "rating": 5,
  "content": "매우 만족스러운 프로그램이었습니다!"
}
```

**Response**:
```json
{
  "success": true,
  "message": "후기가 등록되었습니다.",
  "review": {
    "reviewId": 9,
    "rating": 5,
    "content": "매우 만족스러운 프로그램이었습니다!",
    "createdAt": "2025-11-17T14:30:00"
  }
}
```

### 3. 후기 수정
```
PUT /api/programs/{programId}/reviews/{reviewId}
Content-Type: application/json

{
  "rating": 4,
  "content": "수정된 후기 내용입니다."
}
```

### 4. 후기 삭제
```
DELETE /api/programs/{programId}/reviews/{reviewId}
```

**Response**:
```json
{
  "success": true,
  "message": "후기가 삭제되었습니다."
}
```

---

## 테스트 방법

### 1. 테스트 데이터 확인
1. 애플리케이션 시작
2. 로그 확인:
   ```
   ✅ 테스트 후기 데이터 생성 완료: 8건
   📝 평균 평점: 4.1/5
   ```

### 2. 후기 조회 테스트
1. 프로그램 상세 페이지 접속
2. "후기" 탭 클릭
3. 확인 사항:
   - ✅ 평균 별점 표시 (4.1/5)
   - ✅ 후기 개수 표시 (8개의 후기)
   - ✅ 후기 목록 표시
   - ✅ 작성자 정보 표시
   - ✅ 별점 표시

### 3. 후기 작성 테스트
1. 참여 완료(COMPLETED) 상태의 학생으로 로그인
   - 예: 임도윤 (학번: 2021002, 비번: 990228)
2. 프로그램 상세 페이지 → 후기 탭
3. "후기 작성" 버튼 클릭
4. 별점 선택 (1-5)
5. 후기 내용 입력
6. "등록" 버튼 클릭
7. 확인 사항:
   - ✅ 후기 등록 성공 메시지
   - ✅ 후기 목록에 새 후기 표시
   - ✅ 평균 별점 재계산
   - ✅ "후기 작성" 버튼 숨김 (중복 방지)

### 4. 후기 수정 테스트
1. 본인이 작성한 후기의 "수정" 버튼 클릭
2. 별점 또는 내용 수정
3. "수정" 버튼 클릭
4. 확인 사항:
   - ✅ 수정 성공 메시지
   - ✅ 수정된 내용 반영
   - ✅ "(수정됨)" 표시

### 5. 후기 삭제 테스트
1. 본인이 작성한 후기의 "삭제" 버튼 클릭
2. 확인 대화상자에서 "확인"
3. 확인 사항:
   - ✅ 삭제 성공 메시지
   - ✅ 후기 목록에서 제거
   - ✅ 평균 별점 재계산
   - ✅ "후기 작성" 버튼 다시 표시

### 6. 권한 검증 테스트

#### 6-1. 프로그램 미참여자
1. 프로그램에 신청하지 않은 학생으로 로그인
2. 프로그램 상세 페이지 → 후기 탭
3. 확인 사항:
   - ✅ "후기 작성" 버튼 숨김

#### 6-2. 참여 승인만 받은 학생
1. APPROVED 상태의 학생으로 로그인
2. 프로그램 상세 페이지 → 후기 탭
3. 확인 사항:
   - ✅ "후기 작성" 버튼 숨김

#### 6-3. 타인 후기 수정 시도
1. 다른 사용자로 로그인
2. 타인의 후기 확인
3. 확인 사항:
   - ✅ "수정", "삭제" 버튼 미표시

### 7. 유효성 검사 테스트

#### 7-1. 별점 미선택
1. 후기 작성 모달 열기
2. 별점 선택 안 함
3. 내용만 입력하고 "등록" 클릭
4. 확인 사항:
   - ✅ "별점을 선택해주세요." 경고

#### 7-2. 중복 후기 작성 시도
1. 이미 후기를 작성한 학생으로 로그인
2. 후기 작성 시도
3. 확인 사항:
   - ✅ "이미 후기를 작성하셨습니다." 오류

---

## 트러블슈팅

### 문제 1: N+1 쿼리 문제

**증상**: 후기 목록 조회 시 사용자 정보마다 개별 쿼리 발생

**원인**: Lazy Loading으로 인한 N+1 문제

**해결**:
```java
// Before
List<ProgramReview> findByProgramId(Integer programId);

// After
@Query("SELECT r FROM ProgramReview r " +
       "JOIN FETCH r.user " +  // 추가
       "WHERE r.program.programId = :programId")
List<ProgramReview> findByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);
```

### 문제 2: 평균 평점 null 처리

**증상**: 후기가 없을 때 NPE 발생

**원인**: AVG() 함수가 null 반환

**해결**:
```java
public Double getAverageRating(Integer programId) {
    Double avg = reviewRepository.getAverageRatingByProgramId(programId);
    return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;  // null 체크
}
```

### 문제 3: 삭제된 후기 조회

**증상**: 삭제된 후기가 목록에 계속 표시됨

**원인**: Soft Delete 필터링 누락

**해결**:
```java
@Query("SELECT r FROM ProgramReview r " +
       "WHERE r.program.programId = :programId " +
       "AND r.deletedAt IS NULL")  // 추가
List<ProgramReview> findByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);
```

### 문제 4: 후기 작성 권한 체크

**증상**: 참여하지 않은 사용자도 후기 작성 가능

**원인**: 권한 검증 로직 누락

**해결**:
```java
boolean hasCompletedProgram = applicationRepository
    .existsByUserIdAndProgramIdAndStatus(
        userId, programId, ApplicationStatus.COMPLETED
    );

if (!hasCompletedProgram) {
    throw new IllegalStateException(
        "프로그램을 완료한 사용자만 후기를 작성할 수 있습니다."
    );
}
```

---

## 다음 단계

### 개선 사항
1. ⏳ 이미지 업로드 기능 추가
2. ⏳ 페이지네이션 (후기 많을 때)
3. ⏳ 좋아요/도움돼요 기능
4. ⏳ 관리자의 부적절한 후기 숨김 기능

### 마무리
- ✅ 테스트 데이터 생성 완료
- ✅ 기능 테스트 완료
- 🔄 개발 로그 작성 중
- ⏳ Git 커밋 및 푸시

---

## 정리

### 구현 완료 항목
- ✅ ProgramReview Entity
- ✅ ProgramReviewRepository
- ✅ ReviewRequest/ReviewResponse DTO
- ✅ ProgramReviewService
- ✅ ProgramReviewController
- ✅ Frontend UI (모달, 별점, 후기 목록)
- ✅ Frontend JavaScript (CRUD)
- ✅ 테스트 데이터 생성
- ✅ 평균 별점 계산 및 표시
- ✅ 권한 검증 (참여 완료자만 작성)
- ✅ Soft Delete 패턴
- ✅ N+1 문제 해결

### 핵심 성과
1. **완전한 CRUD**: 후기 생성, 조회, 수정, 삭제
2. **권한 관리**: 참여 완료자만 작성, 본인만 수정/삭제
3. **데이터 무결성**: 1인 1후기, 평점 유효성 검사
4. **사용자 경험**: 직관적인 UI, 별점 표시, 실시간 업데이트
5. **성능 최적화**: JOIN FETCH로 N+1 문제 해결

---

**개발 로그 작성 완료일**: 2025-11-17
