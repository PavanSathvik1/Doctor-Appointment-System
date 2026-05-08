-- V7__create_prescription_items_table.sql
-- Creates the prescription items table linking drugs to a given prescription parent

CREATE TABLE prescription_items (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id   BIGINT NOT NULL,
    medicine_name     VARCHAR(200) NOT NULL,
    dosage            VARCHAR(100) NOT NULL,
    frequency         VARCHAR(100) NOT NULL,
    duration_days     INT NOT NULL,
    instructions      TEXT,

    CONSTRAINT fk_prescription_items_parent
        FOREIGN KEY (prescription_id) REFERENCES prescriptions(id)
        ON DELETE CASCADE,
        
    INDEX idx_prescription_items_parent (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
