-- V4__create_doctor_availability_table.sql
-- Creates the doctor_availability table for scheduling

CREATE TABLE doctor_availability (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id             BIGINT NOT NULL,
    day_of_week           ENUM('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN') NOT NULL,
    start_time            TIME NOT NULL,
    end_time              TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_availability_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id)
        ON DELETE CASCADE,

    INDEX idx_availability_doctor_day (doctor_id, day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
