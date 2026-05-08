-- V11: Fix old fake meet.hms.com video links → real meet.jit.si links
-- Replaces domain only, keeping the existing room ID path intact
UPDATE appointments
SET meeting_link = REPLACE(meeting_link, 'https://meet.hms.com/', 'https://meet.jit.si/')
WHERE meeting_link LIKE 'https://meet.hms.com/%';
