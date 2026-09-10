-- Hexaq MariaDB DDL (prod). Local Oracle uses schema.sql.
-- Linux: set lower_case_table_names=1 (or match HEXAQ_* as created). App SQL keeps HEXAQ_*.
-- Board tables are MyBatis (not JPA). Hibernate ddl-auto does not create them.

CREATE TABLE IF NOT EXISTS HEXAQ_MEMBER (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    company VARCHAR(200),
    member_type VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',
    address VARCHAR(300),
    job_title VARCHAR(100),
    business_number VARCHAR(20),
    company_name VARCHAR(200),
    ceo_name VARCHAR(50),
    workplace_address VARCHAR(300),
    terms_service CHAR(1) DEFAULT 'N',
    terms_privacy CHAR(1) DEFAULT 'N',
    terms_marketing CHAR(1) DEFAULT 'N',
    terms_corporate CHAR(1) DEFAULT 'N',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    oauth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    oauth_id VARCHAR(100),
    nickname VARCHAR(100),
    profile_image VARCHAR(1000),
    kakao_welcome_sent CHAR(1) DEFAULT 'N',
    kakao_welcome_sent_at TIMESTAMP NULL,
    email_verified CHAR(1) NOT NULL DEFAULT 'N',
    phone_verified CHAR(1) NOT NULL DEFAULT 'N',
    verification_channel VARCHAR(20) DEFAULT 'EMAIL',
    verification_code VARCHAR(20),
    verification_expires TIMESTAMP NULL,
    trust_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS HEXAQ_FREE_BOARD (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    visit_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS HEXAQ_QNA_BOARD (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    solution VARCHAR(20) NOT NULL DEFAULT 'general',
    visit_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS IDX_HEXAQ_QNA_SOLUTION ON HEXAQ_QNA_BOARD (solution, id);

CREATE TABLE IF NOT EXISTS HEXAQ_ARCHIVE_BOARD (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    visit_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS HEXAQ_BOARD_FILE (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    file_type VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS HEXAQ_COMMENT (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS HEXAQ_KAKAO_FRIEND (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    kakao_id VARCHAR(100) NOT NULL,
    nickname VARCHAR(100),
    profile_image VARCHAR(1000),
    favorite CHAR(1) DEFAULT 'N',
    CONSTRAINT uk_kakao_friend UNIQUE (member_id, kakao_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS HEXAQ_BOARD_LIKE (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    board_type VARCHAR(20) NOT NULL,
    board_id BIGINT NOT NULL,
    member_login_id VARCHAR(100) NOT NULL,
    CONSTRAINT uk_board_like UNIQUE (board_type, board_id, member_login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
