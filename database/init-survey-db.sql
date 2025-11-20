-- Survey Service Database Initialization
-- 설문조사 서비스 데이터베이스 초기화 스크립트

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS scms_survey
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 권한 부여
GRANT ALL PRIVILEGES ON scms_survey.* TO 'root'@'%';
FLUSH PRIVILEGES;

USE scms_survey;

-- Survey Service 테이블은 JPA ddl-auto=update로 자동 생성됩니다.
