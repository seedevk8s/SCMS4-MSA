-- ============================================
-- V1: Initial Database Schema
-- 사용자, 로그인 이력, 상담사 테이블 생성
-- ============================================

-- 사용자 테이블 (학생, 상담사, 관리자 통합)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID',
    student_num INT NOT NULL UNIQUE COMMENT '학번',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    phone VARCHAR(100) COMMENT '전화번호',
    password VARCHAR(255) COMMENT '비밀번호 (BCrypt 암호화)',
    birth_date DATE NOT NULL COMMENT '생년월일',
    department VARCHAR(100) COMMENT '학과',
    grade INT COMMENT '학년',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '역할 (STUDENT, COUNSELOR, ADMIN)',
    locked TINYINT(1) NOT NULL DEFAULT 0 COMMENT '계정 잠금 여부',
    fail_cnt INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_student_num (student_num),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보';

-- 로그인 히스토리 테이블
CREATE TABLE IF NOT EXISTS login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id INT NOT NULL COMMENT '사용자 ID',
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '로그인 시간',
    ip_address VARCHAR(50) COMMENT 'IP 주소',
    user_agent VARCHAR(255) COMMENT 'User Agent',
    success TINYINT(1) NOT NULL COMMENT '성공 여부',
    fail_reason VARCHAR(100) COMMENT '실패 사유',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='로그인 이력';

-- 상담사 테이블
-- @MapsId 사용: counselor_id = user_id (1:1 관계)
CREATE TABLE IF NOT EXISTS counselors (
    counselor_id INT PRIMARY KEY COMMENT '상담사 ID (= user_id)',
    special VARCHAR(100) COMMENT '전문 분야',
    intro TEXT COMMENT '소개',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    FOREIGN KEY (counselor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담사 정보';

-- 알림 테이블
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '알림 ID',
    user_id INT NOT NULL COMMENT '수신 사용자 ID',
    title VARCHAR(200) NOT NULL COMMENT '알림 제목',
    content TEXT NOT NULL COMMENT '알림 내용',
    type VARCHAR(50) NOT NULL COMMENT '알림 타입 (SYSTEM, CONSULTATION, PROGRAM, MILEAGE, ANNOUNCEMENT)',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '읽음 여부',
    related_url VARCHAR(500) COMMENT '관련 URL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    read_at DATETIME COMMENT '읽은 일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림';
