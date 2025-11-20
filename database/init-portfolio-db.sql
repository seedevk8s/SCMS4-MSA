-- Portfolio Service Database Initialization
-- 포트폴리오 서비스 데이터베이스 초기화 스크립트

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS scms_portfolio
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 권한 부여
GRANT ALL PRIVILEGES ON scms_portfolio.* TO 'root'@'%';
FLUSH PRIVILEGES;

USE scms_portfolio;

-- Portfolio Service 테이블은 JPA ddl-auto=update로 자동 생성됩니다.
-- 이 스크립트는 데이터베이스만 생성합니다.
