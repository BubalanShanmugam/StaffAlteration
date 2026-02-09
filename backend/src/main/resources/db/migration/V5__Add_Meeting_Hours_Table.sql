-- V5__Add_Meeting_Hours_Table.sql
-- Create table for storing meeting hours for staff attendance

CREATE TABLE IF NOT EXISTS attendance_meeting_hours (
    attendance_id BIGINT NOT NULL REFERENCES attendance(id) ON DELETE CASCADE,
    period_number INTEGER NOT NULL,
    PRIMARY KEY (attendance_id, period_number)
);

-- Add meeting status to attendance status if not exists
-- PostgreSQL doesn't have ALTER TYPE IF NOT EXISTS, so we handle this in the entity
