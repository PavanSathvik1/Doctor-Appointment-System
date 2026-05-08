-- V6__create_prescriptions_table.sql
-- Creates the parent prescriptions table attached to an appointment

CREATE TABLE prescriptions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id   BIGINT NOT NULL UNIQUE, -- 1:1 mapping constraint
    patient_id       BIGINT NOT NULL,
    doctor_id        BIGINT NOT NULL,
    diagnosis        TEXT NOT NULL,
    notes            TEXT,
    pdf_s3_key       VARCHAR(500),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prescription_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_prescription_patient
        FOREIGN KEY (patient_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_prescription_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id)
        ON DELETE CASCADE,

    INDEX idx_prescriptions_patient (patient_id),
    INDEX idx_prescriptions_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
