-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64),
    role VARCHAR(16) NOT NULL,
    avatar VARCHAR(256),
    phone VARCHAR(32),
    email VARCHAR(128),
    company VARCHAR(128),
    position VARCHAR(64),
    skills VARCHAR(512),
    experience VARCHAR(32),
    education VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 岗位表
CREATE TABLE IF NOT EXISTS job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    company VARCHAR(128) NOT NULL,
    city VARCHAR(32),
    salary_min INT,
    salary_max INT,
    experience VARCHAR(32),
    education VARCHAR(32),
    tags VARCHAR(512),
    description TEXT,
    hr_id BIGINT,
    hr_name VARCHAR(64),
    status VARCHAR(16) DEFAULT 'OPEN',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 简历投递表
CREATE TABLE IF NOT EXISTS resume (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    job_id BIGINT,
    job_title VARCHAR(128),
    company VARCHAR(128),
    status VARCHAR(16) DEFAULT 'PENDING',
    name VARCHAR(64),
    phone VARCHAR(32),
    skills VARCHAR(512),
    self_intro TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表（在线对话）
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(128),
    from_user BIGINT,
    to_user BIGINT,
    content TEXT,
    is_read INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
