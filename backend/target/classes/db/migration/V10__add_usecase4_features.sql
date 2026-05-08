-- Add consultation mode to doctors
ALTER TABLE doctors ADD COLUMN mode VARCHAR(20) DEFAULT 'OFFLINE' NOT NULL;

-- Add consultation mode and artifacts to appointments
ALTER TABLE appointments ADD COLUMN mode VARCHAR(20) DEFAULT 'OFFLINE' NOT NULL;
ALTER TABLE appointments ADD COLUMN meeting_link VARCHAR(500);
ALTER TABLE appointments ADD COLUMN clinic_address TEXT;

-- Add payment fields to appointments
ALTER TABLE appointments ADD COLUMN payment_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL;
ALTER TABLE appointments ADD COLUMN razorpay_order_id VARCHAR(100);
ALTER TABLE appointments ADD COLUMN razorpay_payment_id VARCHAR(100);
