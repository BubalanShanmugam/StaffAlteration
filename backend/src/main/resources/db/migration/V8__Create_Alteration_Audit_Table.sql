-- V5__Create_Alteration_Audit_Table.sql
-- Migration to create the alteration_audit table for tracking alteration records

CREATE TABLE IF NOT EXISTS alteration_audit (
    id BIGSERIAL PRIMARY KEY,
    
    -- Original Staff Information
    original_staff_id BIGINT,
    original_staff_name VARCHAR(255),
    original_staff_email VARCHAR(255),
    
    -- Date and Absence Details
    absence_date DATE,
    absence_type VARCHAR(50),
    class_name VARCHAR(100),
    subject VARCHAR(255),
    period_number INTEGER,
    
    -- Substitution Details for First Substitute
    first_substitute_id BIGINT,
    first_substitute_name VARCHAR(255),
    first_substitute_email VARCHAR(255),
    first_substitute_status VARCHAR(50), -- PENDING, ACCEPTED, REJECTED
    first_substitute_response_time TIMESTAMP,
    
    -- Substitution Details for Second Substitute
    second_substitute_id BIGINT,
    second_substitute_name VARCHAR(255),
    second_substitute_email VARCHAR(255),
    second_substitute_status VARCHAR(50), -- PENDING, ACCEPTED, REJECTED, NOT_ASSIGNED
    second_substitute_response_time TIMESTAMP,
    
    -- Audit Trail
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    final_status VARCHAR(50), -- FULFILLED, PENDING, CANCELLED
    remarks TEXT,
    
    -- Indexes for efficient querying
    CONSTRAINT fk_original_staff FOREIGN KEY (original_staff_id) REFERENCES staff(id),
    CONSTRAINT fk_first_substitute FOREIGN KEY (first_substitute_id) REFERENCES staff(id),
    CONSTRAINT fk_second_substitute FOREIGN KEY (second_substitute_id) REFERENCES staff(id)
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_alteration_audit_original_staff_id ON alteration_audit(original_staff_id);
CREATE INDEX IF NOT EXISTS idx_alteration_audit_absence_date ON alteration_audit(absence_date);
CREATE INDEX IF NOT EXISTS idx_alteration_audit_final_status ON alteration_audit(final_status);
CREATE INDEX IF NOT EXISTS idx_alteration_audit_created_at ON alteration_audit(created_at);
CREATE INDEX IF NOT EXISTS idx_alteration_audit_first_substitute_id ON alteration_audit(first_substitute_id);
CREATE INDEX IF NOT EXISTS idx_alteration_audit_second_substitute_id ON alteration_audit(second_substitute_id);
