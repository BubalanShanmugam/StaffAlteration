-- V10: Sync ACTIVE timetable_template entries into the timetable table.
-- The substitution / alteration algorithm (triggerAlterationProcess) queries the
-- 'timetable' table, but all live timetable data is created through the
-- timetable_template workflow.  This migration seeds timetable rows from every
-- currently-ACTIVE template so existing data immediately works.
--
-- Going forward, TimetableTemplateService also upserts into 'timetable' whenever
-- a template is saved or activated, keeping the tables in sync automatically.

INSERT INTO timetable (staff_id, subject_id, class_id, day_order, period_number, created_at, updated_at)
SELECT
    tt.staff_id,
    tt.subject_id,
    c.id   AS class_id,
    tt.day_order,
    tt.period_number,
    NOW()  AS created_at,
    NOW()  AS updated_at
FROM timetable_template tt
JOIN class c ON c.class_code = tt.class_code
WHERE tt.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1
      FROM timetable t
      WHERE t.staff_id    = tt.staff_id
        AND t.day_order    = tt.day_order
        AND t.period_number = tt.period_number
  );
