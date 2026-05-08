-- V1__create_users_table.sql
-- Creates the core users table with role-based access control

CREATE TABLE users (
    id             BIGINT         AUTO_INCREMENT PRIMARY KEY,
    email          VARCHAR(255)   NOT NULL UNIQUE,
    password_hash  VARCHAR(255)   NOT NULL,
    role           ENUM('ADMIN', 'DOCTOR', 'PATIENT') NOT NULL,
    status         ENUM('PENDING', 'ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed a default admin account (password: Admin@123 — BCrypt hash)
INSERT INTO users (email, password_hash, role, status)
VALUES ('admin@hms.com', '$2a$12$LJ3a3y8v0mSdXZfgGOEjPe6y8LlGiUV4A7wGQp3xVbIxMZAaf4YOy', 'ADMIN', 'ACTIVE');
