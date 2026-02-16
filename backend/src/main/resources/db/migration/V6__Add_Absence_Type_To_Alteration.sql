-- Add absence_type column to alteration table
ALTER TABLE alteration ADD COLUMN absence_type VARCHAR(20) NOT NULL DEFAULT 'FN';

-- Create check constraint for valid absence types
ALTER TABLE alteration ADD CONSTRAINT check_absence_type 
  CHECK (absence_type IN ('FN', 'AN', 'AF'));

-- Create index for querying by absence type
CREATE INDEX idx_alteration_absence_type ON alteration(absence_type);
