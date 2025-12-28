-- V4__Add_Missing_Columns.sql
-- Add day_type column to attendance if it doesn't exist
ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS day_type VARCHAR(50) NOT NULL DEFAULT 'FULL_DAY';

-- Add missing columns to lesson_plan if they don't exist
ALTER TABLE lesson_plan 
ADD COLUMN IF NOT EXISTS staff_id BIGINT REFERENCES staff(id) ON DELETE CASCADE;

ALTER TABLE lesson_plan 
ADD COLUMN IF NOT EXISTS class_room_id BIGINT REFERENCES class(id) ON DELETE CASCADE;

ALTER TABLE lesson_plan 
ADD COLUMN IF NOT EXISTS subject_id BIGINT REFERENCES subject(id) ON DELETE CASCADE;

ALTER TABLE lesson_plan 
ADD COLUMN IF NOT EXISTS lesson_date DATE;

ALTER TABLE lesson_plan 
ADD COLUMN IF NOT EXISTS original_file_name VARCHAR(255);

ALTER TABLE lesson_plan 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
