-- V1__Create_Initial_Schema.sql
-- Role Table
CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    role_type VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- User Account Table
CREATE TABLE IF NOT EXISTS user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User-Role Mapping
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Department Table
CREATE TABLE IF NOT EXISTS department (
    id SERIAL PRIMARY KEY,
    department_code VARCHAR(50) NOT NULL UNIQUE,
    department_name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- Subject Table
CREATE TABLE IF NOT EXISTS subject (
    id SERIAL PRIMARY KEY,
    subject_code VARCHAR(50) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL REFERENCES department(id) ON DELETE CASCADE
);

-- Class Table
CREATE TABLE IF NOT EXISTS class (
    id SERIAL PRIMARY KEY,
    class_code VARCHAR(50) NOT NULL UNIQUE,
    class_name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL REFERENCES department(id) ON DELETE CASCADE
);

-- Staff Table
CREATE TABLE IF NOT EXISTS staff (
    id SERIAL PRIMARY KEY,
    staff_id VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    department_id BIGINT NOT NULL REFERENCES department(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL UNIQUE REFERENCES user_account(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Timetable Table
CREATE TABLE IF NOT EXISTS timetable (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    day_order INTEGER NOT NULL,
    period_number INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Attendance Table
CREATE TABLE IF NOT EXISTS attendance (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PRESENT',
    day_type VARCHAR(50) NOT NULL DEFAULT 'FULL_DAY',
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(staff_id, attendance_date)
);

-- Lesson Plan Table
CREATE TABLE IF NOT EXISTS lesson_plan (
    id SERIAL PRIMARY KEY,
    file_path VARCHAR(255) NOT NULL,
    notes VARCHAR(1000),
    file_type VARCHAR(50),
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Alteration Table
CREATE TABLE IF NOT EXISTS alteration (
    id SERIAL PRIMARY KEY,
    timetable_id BIGINT NOT NULL REFERENCES timetable(id) ON DELETE CASCADE,
    original_staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    substitute_staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    alteration_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    lesson_plan_id BIGINT REFERENCES lesson_plan(id) ON DELETE SET NULL,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Notification Table
CREATE TABLE IF NOT EXISTS notification (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    alteration_id BIGINT REFERENCES alteration(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Workload Summary Table
CREATE TABLE IF NOT EXISTS workload_summary (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    workload_date DATE NOT NULL,
    total_hours INTEGER NOT NULL,
    regular_hours INTEGER NOT NULL,
    alteration_hours INTEGER NOT NULL,
    weekly_total INTEGER NOT NULL,
    updated_at DATE
);

-- Indexes for Performance
CREATE INDEX idx_staff_user_id ON staff(user_id);
CREATE INDEX idx_timetable_staff_id ON timetable(staff_id);
CREATE INDEX idx_timetable_day_period ON timetable(day_order, period_number);
CREATE INDEX idx_attendance_staff_date ON attendance(staff_id, attendance_date);
CREATE INDEX idx_alteration_date ON alteration(alteration_date);
CREATE INDEX idx_alteration_original_staff ON alteration(original_staff_id);
CREATE INDEX idx_alteration_substitute_staff ON alteration(substitute_staff_id);
CREATE INDEX idx_notification_staff_id ON notification(staff_id);
CREATE INDEX idx_workload_summary_staff_date ON workload_summary(staff_id, workload_date);
