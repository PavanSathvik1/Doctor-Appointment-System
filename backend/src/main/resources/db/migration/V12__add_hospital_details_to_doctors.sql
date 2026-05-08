-- V12: Add hospital name and clinic address to doctors table
ALTER TABLE doctors ADD COLUMN hospital_name VARCHAR(255);
ALTER TABLE doctors ADD COLUMN clinic_address TEXT;
