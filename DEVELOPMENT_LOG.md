# 개발 로그 - 알림 시스템 및 파일 첨부 시스템 구현

**날짜**: 2025-11-17
**브랜치**: `claude/program-file-attachment-system-011fYVC96GVz1UCmUYAuwmD9`
**작업 시간**: 약 6시간

---

## 1. 작업 개요

### 구현된 기능
1. **알림 시스템**: 사용자 알림 표시 및 관리
2. **프로그램 첨부파일 시스템**: 프로그램에 파일 업로드/다운로드 기능
3. **데이터베이스 마이그레이션**: `notifications`, `program_files` 테이블 자동 생성

---

## 2. 주요 이슈 및 해결 과정

### 이슈 #1: 알림 페이지 무한 로딩 ("알림을 불러오는 중...")

**증상**:
- `/notifications` 페이지 접속 시 "알림을 불러오는 중..." 메시지가 무한 표시
- 알림 목록이 로드되지 않음
- 5-6시간 동안 문제 지속

**시도한 해결 방법들 (실패)**:
1. ~~HTTP 에러 처리 추가~~ (커밋 16bc09c)
2. ~~배열 타입 체크 추가~~ (커밋 33e4650)
3. ~~대량의 디버깅 로그 추가~~ (커밋 650d9dc)
4. ~~Fragment 이름 수정: `scripts` → `script`~~ (커밋 62e759b) - 부분적 해결

**근본 원인**:
- **Thymeleaf layout fragment 구조 오류**
- 잘못된 구조:
  ```html
  <script layout:fragment="script">
      // JavaScript 코드
  </script>
  ```
- Thymeleaf layout은 `<th:block>`을 사용해야 fragment가 작동함
- `<script>` 태그에 직접 `layout:fragment`를 붙이면 무시됨

**최종 해결책** (커밋 5940f27):
```html
<th:block layout:fragment="script">
    <script>
        // JavaScript 코드
    </script>
</th:block>
```

**교훈**:
- JavaScript 로그가 출력되지 않으면 **가장 먼저 스크립트 로드 여부를 확인해야 함**
- 복잡한 원인을 찾기 전에 **기본 구조부터 확인**
- 다른 정상 작동하는 페이지(programs.html)의 구조와 비교 필수

---

### 이슈 #2: Notifications 테이블 스키마 오류

**증상**:
- `content` 컬럼이 NULL 허용으로 생성됨
- DataLoader에서 알림 생성 시 에러 발생

**원인**:
- Hibernate가 엔티티를 보고 테이블을 먼저 생성
- 이후 `@Column(nullable=false)` 제약조건 추가가 반영 안 됨

**해결책**:
1. Notification.java에 `@Column(nullable=false)` 추가 (커밋 8518c23)
2. DatabaseMigration에서 기존 테이블 DROP 후 재생성 (커밋 49f41ca)
3. 테이블 스키마 검증 로직 추가

---

### 이슈 #3: LazyInitializationException

**증상**:
- `/api/notifications` 호출 시 User 엔티티 접근 에러

**원인**:
- `open-in-view: false` 설정
- Transaction 범위 밖에서 LAZY 로딩 시도

**해결책** (커밋 8114010):
```yaml
jpa:
  open-in-view: true  # false → true
```

**대안**:
- NotificationRepository에 JOIN FETCH 사용
- DTO로 변환하여 리턴

---

## 3. 구현된 기능 상세

### 3.1 알림 시스템

**API 엔드포인트**:
- `GET /api/notifications` - 알림 목록 조회
- `GET /api/notifications/unread` - 읽지 않은 알림 조회
- `GET /api/notifications/unread-count` - 읽지 않은 알림 개수
- `PUT /api/notifications/{id}/read` - 알림 읽음 처리
- `PUT /api/notifications/read-all` - 모든 알림 읽음 처리
- `DELETE /api/notifications/{id}` - 알림 삭제
- `DELETE /api/notifications/all` - 모든 알림 삭제

**알림 타입**:
- `APPLICATION_APPROVED` - 신청 승인
- `APPLICATION_REJECTED` - 신청 거부
- `APPLICATION_CANCELLED` - 신청 취소
- `PROGRAM_STARTING` - 프로그램 시작 (D-1)
- `DEADLINE_APPROACHING` - 신청 마감 임박 (D-3)

**주요 기능**:
- 실시간 알림 카운트 (헤더 배지)
- 알림 클릭 시 관련 페이지로 이동
- Soft delete 지원
- 읽음/읽지 않음 상태 관리

---

### 3.2 프로그램 첨부파일 시스템

**API 엔드포인트**:
- `GET /api/programs/{programId}/files` - 파일 목록 조회
- `POST /api/programs/{programId}/files` - 파일 업로드
- `GET /api/programs/{programId}/files/{fileId}/download` - 파일 다운로드
- `DELETE /api/programs/{programId}/files/{fileId}` - 파일 삭제

**지원 파일 형식**:
- 문서: PDF, DOCX, DOC, PPTX, PPT, XLSX, XLS, HWP, TXT
- 이미지: JPG, JPEG, PNG
- 압축: ZIP

**제한 사항**:
- 최대 파일 크기: 10MB
- 관리자만 업로드/삭제 가능
- UUID 기반 파일명으로 저장
- Soft delete 지원

---

### 3.3 데이터베이스 마이그레이션

**DatabaseMigration.java**:
- 애플리케이션 시작 시 자동 실행
- 테이블 존재 여부 확인
- 스키마 검증 및 자동 수정
- 실패 시에도 애플리케이션 계속 실행

**생성 테이블**:

1. **notifications**:
```sql
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    read_at DATETIME,
    deleted_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX (user_id, is_read, deleted_at, created_at)
);
```

2. **program_files**:
```sql
CREATE TABLE program_files (
    file_id INT AUTO_INCREMENT PRIMARY KEY,
    program_id INT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    uploaded_at DATETIME,
    deleted_at DATETIME,
    uploaded_by INT,
    FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX (program_id, deleted_at, uploaded_at)
);
```

---

## 4. 커밋 히스토리 (총 18개)

1. `6c64553` - Implement comprehensive notification system
2. `25be6a3` - Fix notification scheduler
3. `7752c41` - Complete notification system: Add program execution date fields
4. `b1de6ab` - Add database migration for program execution date fields
5. `b6d235f` - Add SQL migration scripts for documentation
6. `10e23b5` - Fix: Add notifications table creation to DatabaseMigration
7. `8518c23` - Fix: Add nullable=false to Notification.content field
8. `d4d5442` - Fix: Auto-repair existing notifications table schema
9. `49f41ca` - Fix: Force recreate notifications table with correct schema
10. `372afb4` - Fix: Disable notification initialization in DataLoader
11. `a79abc1` - Fix: Re-enable notification initialization with correct schema
12. `8114010` - **Fix: Enable open-in-view to resolve LAZY loading issue**
13. `34a2f85` - **Add: Create program_files table in database migration**
14. `16bc09c` - Fix: Add HTTP error handling to notification loading (불필요)
15. `33e4650` - Fix: Add array type check in renderNotifications (불필요)
16. `650d9dc` - Fix: Add comprehensive debugging logs for notification loading (불필요)
17. `62e759b` - Fix: Correct Thymeleaf fragment name from 'scripts' to 'script' (부분 해결)
18. `5940f27` - **Fix: Use th:block for script fragment instead of script tag (최종 해결)** ⭐

---

## 5. 테스트 결과

### 알림 시스템 테스트
- ✅ 알림 목록 로드 성공
- ✅ 알림 개수 표시 (헤더 배지)
- ✅ 알림 읽음 처리
- ✅ 알림 삭제
- ✅ 알림 클릭 시 페이지 이동
- ✅ 빈 상태 표시 ("알림이 없습니다")

**실제 테스트 로그**:
```
[DEBUG] loadNotifications() 시작
[DEBUG] API 응답 받음
[DEBUG] - 상태 코드: 200
[DEBUG] - OK 여부: true
[DEBUG] JSON 파싱 성공
[DEBUG] 배열 여부: true
[DEBUG] 데이터 길이: 1
[DEBUG] 알림 1/1 렌더링 중
[DEBUG] HTML 렌더링 완료
[DEBUG] 생성된 HTML 길이: 1035
```

### 첨부파일 시스템 테스트
- ✅ 파일 업로드 (관리자)
- ✅ 파일 목록 조회
- ✅ 파일 다운로드
- ✅ 파일 삭제 (관리자)
- ✅ 파일 크기/타입 검증

---

## 6. 향후 개선 사항

### 알림 시스템
- [ ] 실시간 알림 (WebSocket 또는 Server-Sent Events)
- [ ] 알림 설정 (타입별 on/off)
- [ ] 알림 히스토리 (30일 이상 보관)
- [ ] 푸시 알림 (브라우저 Notification API)

### 첨부파일 시스템
- [ ] 파일 미리보기 (이미지, PDF)
- [ ] 드래그 앤 드롭 업로드
- [ ] 다중 파일 업로드
- [ ] 파일 다운로드 횟수 추적
- [ ] 클라우드 스토리지 연동 (S3 등)

### 코드 품질
- [ ] 디버깅 로그 제거 (커밋 14-16)
- [ ] 단위 테스트 추가
- [ ] E2E 테스트 추가
- [ ] 성능 최적화 (N+1 쿼리 확인)

---

## 7. 배운 교훈

### 기술적 교훈
1. **Thymeleaf layout 구조 이해 필수**
   - `layout:fragment`는 `<th:block>`과 함께 사용
   - `<script>` 태그에 직접 사용 불가

2. **문제 해결 순서**
   - 로그가 안 보이면 → 스크립트 로드 확인
   - 복잡한 원인보다 기본 구조 먼저 확인
   - 정상 작동하는 코드와 비교

3. **Hibernate 스키마 관리**
   - `ddl-auto`만 믿지 말고 명시적 마이그레이션 작성
   - CommandLineRunner로 스키마 검증/수정
   - NOT NULL 제약조건은 엔티티와 마이그레이션 모두 필요

4. **LAZY 로딩 문제**
   - `open-in-view` vs JOIN FETCH 선택
   - DTO 변환으로 Transaction 범위 문제 회피

### 프로세스 교훈
1. **사용자 피드백 신뢰**
   - "다 했다"고 하면 믿어야 함
   - 반복적인 확인 요청은 시간 낭비

2. **근본 원인 찾기**
   - 증상 치료보다 원인 파악
   - 가장 기본적인 것부터 확인

3. **시간 관리**
   - 30분 내 해결 안 되면 접근 방식 재검토
   - 막히면 처음부터 다시 생각

---

## 8. PR 정보

**PR 링크**: https://github.com/seedevk8s/SCMS3/compare/main...claude/program-file-attachment-system-011fYVC96GVz1UCmUYAuwmD9

**리뷰어 확인 사항**:
- [ ] 알림 시스템 정상 작동 확인
- [ ] 첨부파일 업로드/다운로드 테스트
- [ ] 데이터베이스 마이그레이션 로그 확인
- [ ] 커밋 14-16 squash 검토 (불필요한 디버깅 로그)

**주의사항**:
- 프로덕션 배포 전 디버깅 로그 제거 권장
- `open-in-view: true` 설정이 성능에 미칠 영향 검토
- 파일 업로드 디렉토리 권한 확인 필요

---

## 9. 총평

**작업 시간**: 약 6시간
**실제 필요 시간**: 30분 (근본 원인 파악 후)
**시간 낭비 원인**: Thymeleaf fragment 구조 미확인

**최종 결과**: ✅ 성공
- 알림 시스템 정상 작동
- 첨부파일 시스템 정상 작동
- 데이터베이스 마이그레이션 완료

**반성**:
- 기본 구조부터 확인했어야 함
- 사용자 피드백을 더 신뢰했어야 함
- 불필요한 커밋 16개 생성

---

**작성일**: 2025-11-17
**작성자**: Claude (AI Assistant)
