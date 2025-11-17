# 다음 세션 컨텍스트

## 현재 세션 완료 사항

### 1. 알림 시스템 구현 완료 ✅
- **백엔드**: Notification 엔티티, Repository, Service, Controller 완전 구현
- **프론트엔드**: notifications.html 페이지 완전 구현
- **헤더 통합**: 알림 아이콘 및 미읽음 배지 추가
- **상태**: 정상 작동 확인 완료

### 2. 프로그램 첨부파일 시스템 데이터베이스 준비 ✅
- **테이블**: program_files 테이블 생성 완료
- **마이그레이션**: DatabaseMigration.java에 추가 완료
- **외래키**: program_id, uploaded_by 제약조건 설정 완료
- **소프트 삭제**: deleted_at 컬럼 포함

### 3. 핵심 버그 수정 ✅
- **문제**: JavaScript가 로딩되지 않아 알림 페이지가 "알림을 불러오는 중..."에서 멈춤
- **원인**: Thymeleaf fragment 구조 오류
  - ❌ 잘못된 구조: `<script layout:fragment="script">...</script>`
  - ✅ 올바른 구조: `<th:block layout:fragment="script"><script>...</script></th:block>`
- **해결**: Commit 5940f27에서 수정 완료
- **디버깅 소요 시간**: 6시간

### 4. 문서화 완료 ✅
- **개발 로그**: `doc/development-logs/17_NOTIFICATION_AND_FILE_SYSTEM_COMPLETE_REFLECTION.md`
- **내용**:
  - 전체 구현 과정 상세 기록
  - 6시간 디버깅 과정 완전 기록
  - 9가지 실수 항목 분석 및 반성
  - 향후 방지 대책 수립
- **분량**: 885줄

### 5. Git 작업 완료 ✅
- **브랜치**: `claude/program-file-attachment-system-011fYVC96GVz1UCmUYAuwmD9`
- **총 커밋 수**: 23개
- **푸시 상태**: 모든 커밋 원격 저장소에 푸시 완료
- **PR 링크**: https://github.com/seedevk8s/SCMS3/compare/main...claude/program-file-attachment-system-011fYVC96GVz1UCmUYAuwmD9

## 주요 파일 위치

### 백엔드
- `src/main/java/com/scms/app/model/Notification.java` - 알림 엔티티
- `src/main/java/com/scms/app/model/NotificationType.java` - 알림 타입 enum
- `src/main/java/com/scms/app/repository/NotificationRepository.java` - 알림 레포지토리
- `src/main/java/com/scms/app/service/NotificationService.java` - 알림 서비스
- `src/main/java/com/scms/app/controller/NotificationController.java` - 알림 컨트롤러
- `src/main/java/com/scms/app/scheduler/NotificationScheduler.java` - 알림 스케줄러
- `src/main/java/com/scms/app/util/DatabaseMigration.java` - program_files 테이블 마이그레이션

### 프론트엔드
- `src/main/resources/templates/notifications.html` - 알림 페이지 (✅ th:block 구조 수정 완료)
- `src/main/resources/templates/layout/header.html` - 헤더 알림 아이콘

### 설정
- `src/main/resources/application.yml` - `open-in-view: true` 설정

### 문서
- `doc/development-logs/17_NOTIFICATION_AND_FILE_SYSTEM_COMPLETE_REFLECTION.md` - 완전한 개발 로그 및 반성문

## 현재 시스템 상태

### 작동 중인 기능
1. ✅ 알림 시스템 완전 작동
2. ✅ 알림 페이지 정상 로딩
3. ✅ JavaScript 정상 실행
4. ✅ 헤더 알림 아이콘 표시
5. ✅ program_files 테이블 존재

### 데이터베이스 테이블
- `notifications` - 알림 데이터
- `program_files` - 프로그램 첨부파일 메타데이터 (생성 완료, 백엔드 미구현)

## 🎯 다음 개발 우선순위

### 📌 1순위: 프로그램 첨부파일 시스템 백엔드 구현 (CRITICAL)

**현재 상태**: program_files 테이블만 생성됨, 백엔드 로직 전혀 없음

**구현 순서**:

#### Step 1: ProgramFile 엔티티 생성
- **파일 위치**: `src/main/java/com/scms/app/model/ProgramFile.java`
- **참고**: `Notification.java` 구조 참고
- **필수 필드**:
  - `fileId` (PK, AUTO_INCREMENT)
  - `programId` (FK to programs)
  - `originalFileName` (사용자가 업로드한 원본 파일명)
  - `storedFileName` (서버에 저장된 파일명, UUID 사용)
  - `filePath` (실제 저장 경로)
  - `fileSize` (바이트 단위)
  - `fileType` (MIME type)
  - `uploadedAt` (업로드 시각)
  - `deletedAt` (소프트 삭제, nullable)
  - `uploadedBy` (FK to users)
- **관계 설정**:
  - `@ManyToOne` with `Program`
  - `@ManyToOne` with `User` (uploader)

#### Step 2: ProgramFileRepository 생성
- **파일 위치**: `src/main/java/com/scms/app/repository/ProgramFileRepository.java`
- **참고**: `NotificationRepository.java` 구조 참고
- **필수 메서드**:
  ```java
  List<ProgramFile> findByProgramIdAndDeletedAtIsNull(Integer programId);
  Optional<ProgramFile> findByFileIdAndDeletedAtIsNull(Integer fileId);
  @Query("SELECT pf FROM ProgramFile pf WHERE pf.programId = :programId AND pf.deletedAt IS NULL ORDER BY pf.uploadedAt DESC")
  List<ProgramFile> findActiveProgramFiles(@Param("programId") Integer programId);
  ```

#### Step 3: 파일 저장 설정 (application.yml)
- **파일 위치**: `src/main/resources/application.yml`
- **추가 설정**:
  ```yaml
  file:
    upload-dir: ${user.home}/scms-uploads/programs  # 프로그램 첨부파일 저장 경로
    max-size: 10485760  # 10MB (바이트 단위)
    allowed-extensions: pdf,doc,docx,xls,xlsx,ppt,pptx,zip,jpg,png
  ```
- **주의**: 개발 환경에서는 절대 경로 사용, 프로덕션에서는 환경변수로 관리

#### Step 4: ProgramFileService 구현
- **파일 위치**: `src/main/java/com/scms/app/service/ProgramFileService.java`
- **참고**: `NotificationService.java` 구조 참고
- **필수 메서드**:
  1. `uploadFile(Integer programId, MultipartFile file, Integer uploaderId)` - 파일 업로드
     - UUID로 고유한 파일명 생성
     - 확장자 검증 (allowed-extensions)
     - 파일 크기 검증 (max-size)
     - 실제 파일 시스템에 저장
     - DB에 메타데이터 저장
     - 트랜잭션 처리 (파일 저장 실패 시 롤백)

  2. `getFilesByProgramId(Integer programId)` - 프로그램의 첨부파일 목록 조회

  3. `downloadFile(Integer fileId)` - 파일 다운로드
     - 파일 존재 여부 확인
     - 파일 스트림 반환

  4. `deleteFile(Integer fileId, Integer userId)` - 파일 삭제 (소프트 삭제)
     - 권한 체크 (업로더 본인 or 관리자만)
     - deletedAt 설정
     - 실제 파일은 유지 (복구 가능성)

- **예외 처리**:
  - `FileStorageException` - 파일 저장 실패
  - `FileNotFoundException` - 파일 없음
  - `InvalidFileException` - 잘못된 파일 형식/크기
  - `UnauthorizedException` - 권한 없음

#### Step 5: ProgramFileController 구현
- **파일 위치**: `src/main/java/com/scms/app/controller/ProgramFileController.java`
- **참고**: `NotificationController.java` 구조 참고
- **필수 엔드포인트**:
  1. `POST /api/programs/{programId}/files` - 파일 업로드
     - 요청: `MultipartFile`
     - 응답: `{ "success": true, "fileId": 123, "fileName": "..." }`

  2. `GET /api/programs/{programId}/files` - 파일 목록 조회
     - 응답: `List<ProgramFileResponse>`

  3. `GET /api/files/{fileId}/download` - 파일 다운로드
     - 응답: `ResponseEntity<Resource>` with Content-Disposition header

  4. `DELETE /api/files/{fileId}` - 파일 삭제
     - 응답: `{ "success": true }`

- **보안**:
  - `@PreAuthorize` 사용하여 인증된 사용자만 접근
  - 파일 다운로드 시 권한 체크 (프로그램 참여자만)

#### Step 6: 파일 저장 디렉토리 자동 생성
- **파일 위치**: `src/main/java/com/scms/app/config/FileStorageConfig.java` (새 파일)
- **구현**:
  ```java
  @Configuration
  public class FileStorageConfig {
      @Value("${file.upload-dir}")
      private String uploadDir;

      @PostConstruct
      public void init() {
          Path uploadPath = Paths.get(uploadDir);
          if (!Files.exists(uploadPath)) {
              Files.createDirectories(uploadPath);
          }
      }
  }
  ```

---

### 📌 2순위: 프로그램 첨부파일 시스템 프론트엔드 구현

**현재 상태**: UI 전혀 없음

**구현 순서**:

#### Step 1: 프로그램 상세 페이지에 첨부파일 섹션 추가
- **파일 위치**: `src/main/resources/templates/programs.html`
- **위치**: 프로그램 상세 정보 아래에 "첨부파일" 섹션 추가
- **구조**:
  ```html
  <th:block layout:fragment="script">
      <script>
      // 첨부파일 관련 JavaScript
      function loadProgramFiles(programId) { ... }
      function uploadFile(programId) { ... }
      function downloadFile(fileId) { ... }
      function deleteFile(fileId) { ... }
      </script>
  </th:block>
  ```
- **주의**: 반드시 `<th:block>` 래퍼 사용!!! (notifications.html 교훈)

#### Step 2: 파일 업로드 UI
- **구성 요소**:
  - `<input type="file" multiple>` - 다중 파일 선택 가능
  - 드래그 앤 드롭 영역 (선택적)
  - 업로드 진행률 표시 (선택적)
  - 파일 크기/형식 제한 안내 문구

#### Step 3: 첨부파일 목록 표시
- **표시 정보**:
  - 파일명 (originalFileName)
  - 파일 크기 (KB/MB 변환)
  - 업로드 일시
  - 업로더 이름
  - 다운로드 버튼
  - 삭제 버튼 (권한 있는 경우만)

#### Step 4: 파일 다운로드 기능
- **구현**:
  ```javascript
  function downloadFile(fileId) {
      window.location.href = `/api/files/${fileId}/download`;
  }
  ```

#### Step 5: 파일 삭제 기능
- **구현**:
  - 삭제 확인 대화상자
  - DELETE API 호출
  - 목록에서 제거 (화면 갱신)

---

### 📌 3순위: 알림 시스템 고도화 (선택적, 나중에)

**현재 상태**: 기본 알림 기능 작동 중

**향후 고도화 항목**:
- 실시간 알림 (WebSocket/SSE)
- 알림 필터링 (타입별, 읽음/안읽음)
- 알림 검색 기능
- 알림 페이지네이션

**우선순위**: 낮음 (1순위, 2순위 완료 후 검토)

---

## ⚠️ 예상 이슈 및 주의사항

### 1. 파일 업로드 크기 제한
- **문제**: Spring Boot 기본 파일 업로드 제한 (1MB)
- **해결**: application.yml에 설정 추가
  ```yaml
  spring:
    servlet:
      multipart:
        max-file-size: 10MB
        max-request-size: 10MB
  ```

### 2. 파일 저장 경로 권한
- **문제**: 애플리케이션이 파일을 저장할 디렉토리 접근 권한 없음
- **해결**:
  - 개발 환경: `${user.home}/scms-uploads` 사용
  - 프로덕션: 별도 볼륨 마운트 또는 클라우드 스토리지

### 3. 파일명 중복
- **문제**: 같은 이름의 파일 업로드 시 덮어쓰기
- **해결**: UUID로 고유한 저장 파일명 생성

### 4. 트랜잭션 처리
- **문제**: 파일은 저장되었는데 DB 저장 실패 (또는 반대)
- **해결**:
  - 파일 저장 후 DB 저장
  - DB 저장 실패 시 저장된 파일 삭제 (롤백)
  - `@Transactional` 사용

### 5. 보안
- **문제**: 악성 파일 업로드, 경로 탐색 공격
- **해결**:
  - 확장자 화이트리스트
  - 파일명 검증 (특수문자 제거)
  - 바이러스 스캔 (선택적)
  - 업로드 사용자 인증 필수

### 6. Thymeleaf Fragment 구조
- **절대 잊지 말 것**: `<th:block layout:fragment="script">` 사용!!!
- **잘못된 구조 사용 시**: JavaScript 로딩 안됨 (6시간 낭비)

## 중요 교훈

### Thymeleaf Fragment 구조
**절대 잊지 말 것**: Thymeleaf에서 `layout:fragment`를 사용할 때는 반드시 `<th:block>` 래퍼를 사용해야 합니다.

```html
<!-- ❌ 잘못된 방법 (JavaScript 로딩 안됨) -->
<script layout:fragment="script">
    // JavaScript code
</script>

<!-- ✅ 올바른 방법 -->
<th:block layout:fragment="script">
    <script>
        // JavaScript code
    </script>
</th:block>
```

### 디버깅 체크리스트
1. JavaScript 콘솔에서 스크립트가 로딩되는지 확인 (Network 탭)
2. 작동하는 페이지와 구조 비교
3. 기본적인 HTML/Thymeleaf 구조 확인
4. 복잡한 문제부터 찾지 말고 단순한 것부터 확인

### 사용자 신뢰
사용자가 "다 했다"고 하면 믿고, 코드 자체의 문제를 먼저 찾을 것.

## Git 정보
- **현재 브랜치**: `claude/program-file-attachment-system-011fYVC96GVz1UCmUYAuwmD9`
- **베이스 브랜치**: `main`
- **최신 커밋**: `8dd0855 - Docs: Simplify next session context to 4 lines`
- **원격 동기화**: ✅ 완료
- **PR 상태**: 생성 대기 (링크 제공 완료)

## 참고 사항
- 이 세션에서는 프로그램 첨부파일 시스템의 데이터베이스 테이블만 생성하고, 실제 파일 업로드/다운로드 기능은 구현하지 않았습니다.
- 알림 시스템은 완전히 작동하며, 사용자가 직접 테스트하여 정상 작동을 확인했습니다.
- 6시간 디버깅 과정에 대한 완전한 반성문이 문서화되어 있습니다.
