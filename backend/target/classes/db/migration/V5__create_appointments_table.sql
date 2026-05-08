-- V5__create_appointments_table.sql
-- Creates the appointments table for booking patient-doctor visits

CREATE TABLE appointments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT NOT NULL,
    doctor_id           BIGINT NOT NULL,
    appointment_date    DATE NOT NULL,
    start_time          TIME NOT NULL,
    end_time            TIME NOT NULL,
    status              ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'PENDING',
    reason_for_visit    TEXT,
    cancellation_reason TEXT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_appointments_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id)
        ON DELETE CASCADE,

    -- Ensure a doctor cannot be double-booked exactly
    CONSTRAINT uq_doctor_time UNIQUE (doctor_id, appointment_date, start_time),
    
    INDEX idx_appointments_patient (patient_id),
    INDEX idx_appointments_doctor (doctor_id),
    INDEX idx_appointments_date_time (appointment_date, start_time),
    INDEX idx_appointments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
