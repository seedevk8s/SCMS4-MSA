# 데이터베이스 유틸리티 스크립트

이 디렉토리에는 개발 및 운영 중 필요한 SQL 스크립트 파일들이 저장되어 있습니다.

## 📁 스크립트 목록

### 상담사 관리
- **add-counselor-8000002.sql**: 상담사 계정 추가 (학번: 8000002)
- **add-counselor.sql**: 초기 상담사 데이터 생성

### 스키마 마이그레이션
- **add_program_dates.sql**: 프로그램 테이블에 날짜 컬럼 추가
- **create_notifications_table.sql**: 알림 테이블 생성
- **fix_notifications_table.sql**: 알림 테이블 구조 수정
- **fix_flyway.sql**: Flyway 마이그레이션 오류 수정

## 🚀 사용 방법

### MySQL 콘솔에서 실행
```bash
# 프로젝트 루트에서 실행
mysql -u root -p scms2 < database/scripts/add-counselor-8000002.sql
```

### MySQL 내에서 실행
```sql
USE scms2;
SOURCE database/scripts/add-counselor-8000002.sql;
```

### Docker 환경에서 실행
```bash
docker exec -i scms2-mysql mysql -uroot -ppassword scms2 < database/scripts/add-counselor-8000002.sql
```

## ⚠️ 주의사항

1. **백업 먼저**: 프로덕션 환경에서는 스크립트 실행 전 반드시 데이터베이스 백업을 수행하세요.
   ```bash
   mysqldump -u root -p scms2 > backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **테스트 환경 검증**: 스키마 변경 스크립트는 테스트 환경에서 먼저 검증하세요.

3. **애플리케이션 재시작**: 스크립트 실행 후 Spring Boot 애플리케이션을 재시작하여 변경사항을 반영하세요.

4. **트랜잭션**: 중요한 데이터 변경 시에는 트랜잭션을 사용하세요.
   ```sql
   START TRANSACTION;
   -- SQL 실행
   COMMIT; -- 또는 ROLLBACK;
   ```

## 📝 스크립트 작성 가이드

새로운 스크립트를 작성할 때는 다음 가이드를 따르세요:

### 파일명 규칙
- 소문자와 하이픈 사용: `add-feature-name.sql`
- 날짜 포함 (옵션): `20251118_add_new_column.sql`

### 스크립트 구조
```sql
-- =========================================
-- 스크립트명: 기능 설명
-- 작성일: YYYY-MM-DD
-- 작성자: 이름
-- 설명: 상세 설명
-- =========================================

-- 1. 기존 데이터 확인
SELECT COUNT(*) FROM table_name;

-- 2. 변경사항 적용
ALTER TABLE table_name ADD COLUMN new_column VARCHAR(100);

-- 3. 결과 확인
DESCRIBE table_name;
```

### 권장사항
- 멱등성(Idempotent): 스크립트를 여러 번 실행해도 안전하도록 작성
  ```sql
  ALTER TABLE table_name
  ADD COLUMN IF NOT EXISTS new_column VARCHAR(100);
  ```
- 롤백 스크립트: 변경사항을 되돌릴 수 있는 롤백 스크립트도 함께 작성
- 주석: 복잡한 쿼리에는 충분한 주석 추가

## 📚 참고

- 메인 스키마 파일: `database/schema.sql`
- 데이터베이스 설정 가이드: `database/README.md`
- 데이터베이스 설계서: `deliverables/02_설계문서/02_데이터베이스설계서.md`

---

**최종 수정일**: 2025-11-18
