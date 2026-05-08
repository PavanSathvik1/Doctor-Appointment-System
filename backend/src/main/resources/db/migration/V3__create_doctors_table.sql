-- V3__create_doctors_table.sql
-- Creates the doctors table, which shares a 1:1 primary key with the users table

CREATE TABLE doctors (
    id               BIGINT         PRIMARY KEY,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    specialisation   VARCHAR(150)   NOT NULL,
    licence_number   VARCHAR(100)   NOT NULL UNIQUE,
    experience_years INT,
    consultation_fee DECIMAL(10,2),
    bio              TEXT,
    document_s3_key  VARCHAR(500),
    approval_status  ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    phone            VARCHAR(20),

    CONSTRAINT fk_doctors_user
        FOREIGN KEY (id) REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_doctors_specialisation (specialisation),
    INDEX idx_doctors_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
