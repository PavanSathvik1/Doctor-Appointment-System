ALTER TABLE users 
ADD COLUMN first_name VARCHAR(100) AFTER id,
ADD COLUMN last_name VARCHAR(100) AFTER first_name;
