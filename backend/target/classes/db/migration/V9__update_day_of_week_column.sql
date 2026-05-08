-- V9__update_day_of_week_column.sql
-- Updates the day_of_week column to support full day names (e.g. MONDAY) 
-- that match Java's standard java.time.DayOfWeek enum values.

ALTER TABLE doctor_availability 
MODIFY COLUMN day_of_week VARCHAR(20) NOT NULL;
