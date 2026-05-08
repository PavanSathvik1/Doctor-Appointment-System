-- V2__create_refresh_tokens_table.sql
-- Stores long-lived refresh tokens for JWT token rotation

CREATE TABLE refresh_tokens (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    token       VARCHAR(500)  NOT NULL UNIQUE,
    expires_at  TIMESTAMP     NOT NULL,
    revoked     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
