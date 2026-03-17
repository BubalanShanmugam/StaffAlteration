-- Expand absence_type check constraint to support per-period absence types (PERIOD_1 to PERIOD_6)
ALTER TABLE alteration DROP CONSTRAINT check_absence_type;

ALTER TABLE alteration ADD CONSTRAINT check_absence_type
  CHECK (absence_type IN ('FN', 'AN', 'AF', 'ONDUTY', 'PERIOD_WISE_ABSENT',
                          'PERIOD_1', 'PERIOD_2', 'PERIOD_3',
                          'PERIOD_4', 'PERIOD_5', 'PERIOD_6'));
