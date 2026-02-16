-- Add period_number column to alteration table
ALTER TABLE alteration ADD COLUMN period_number INTEGER;

-- Add check constraint for valid period numbers (1-6)
ALTER TABLE alteration ADD CONSTRAINT check_period_number 
  CHECK (period_number IS NULL OR (period_number >= 1 AND period_number <= 6));

-- Update the absence type constraint to include new types
ALTER TABLE alteration DROP CONSTRAINT check_absence_type;

ALTER TABLE alteration ADD CONSTRAINT check_absence_type 
  CHECK (absence_type IN ('FN', 'AN', 'AF', 'ONDUTY', 'PERIOD_WISE_ABSENT'));

-- Create index for querying by period number
CREATE INDEX idx_alteration_period_number ON alteration(period_number);
