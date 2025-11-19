-- =========================================
-- 마이그레이션: 소셜 로그인 기능 추가
-- 버전: V10
-- 작성일: 2025-11-18
-- 설명: external_users 테이블에 소셜 로그인 관련 필드 추가
-- =========================================

-- 1. provider 필드 추가 (소셜 로그인 제공자)
ALTER TABLE external_users
ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' COMMENT '로그인 제공자 (LOCAL, GOOGLE, KAKAO, NAVER)'
AFTER password;

-- 2. provider_id 필드 추가 (제공자별 사용자 고유 ID)
ALTER TABLE external_users
ADD COLUMN provider_id VARCHAR(255) COMMENT '소셜 로그인 제공자의 사용자 ID'
AFTER provider;

-- 3. profile_image_url 필드 추가 (프로필 이미지 URL)
ALTER TABLE external_users
ADD COLUMN profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL'
AFTER provider_id;

-- 4. password 필드를 NULL 허용으로 변경 (소셜 로그인 사용자는 비밀번호가 없음)
ALTER TABLE external_users
MODIFY COLUMN password VARCHAR(255) NULL COMMENT '비밀번호 (BCrypt 암호화, 소셜 로그인 시 NULL)';

-- 5. provider와 provider_id에 대한 유니크 인덱스 추가 (동일 제공자에서 중복 가입 방지)
ALTER TABLE external_users
ADD CONSTRAINT uk_provider_provider_id UNIQUE (provider, provider_id);

-- 6. provider 인덱스 추가
ALTER TABLE external_users
ADD INDEX idx_provider (provider);

-- 완료 메시지
SELECT '소셜 로그인 필드 추가 완료!' AS message;
