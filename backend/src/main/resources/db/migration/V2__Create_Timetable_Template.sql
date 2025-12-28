-- Create timetable_template table for new timetable creation module
CREATE TABLE IF NOT EXISTS timetable_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL,
    class_code VARCHAR(50) NOT NULL,
    day_order INTEGER NOT NULL CHECK (day_order >= 1 AND day_order <= 6),
    period_number INTEGER NOT NULL CHECK (period_number >= 1 AND period_number <= 6),
    subject_id BIGINT NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES user_account(id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),
    remarks TEXT,
    version BIGINT DEFAULT 0,
    CONSTRAINT unique_class_slot UNIQUE(class_code, day_order, period_number)
);

-- Create indexes for better query performance
CREATE INDEX idx_timetable_template_class ON timetable_template(class_code);
CREATE INDEX idx_timetable_template_staff ON timetable_template(staff_id);
CREATE INDEX idx_timetable_template_subject ON timetable_template(subject_id);
CREATE INDEX idx_timetable_template_status ON timetable_template(status);
CREATE INDEX idx_timetable_template_day_period ON timetable_template(class_code, day_order, period_number);
CREATE INDEX idx_timetable_template_created_by ON timetable_template(created_by);
