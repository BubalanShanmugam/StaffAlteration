-- V9__Reset_Sequences.sql
-- Reset all PostgreSQL sequences to be ahead of the current max id in each table.
-- This is needed because DataInitializer and direct SQL inserts populated rows with
-- explicit ids without advancing the sequences, causing duplicate key violations on
-- the next ORM insert.
--
-- COALESCE(MAX(id), 0) + 1 handles empty tables (sets sequence to 1).
-- setval(..., value, false) means the NEXT nextval() call returns exactly 'value'.

SELECT setval(pg_get_serial_sequence('role',                'id'), COALESCE((SELECT MAX(id) FROM role),                0) + 1, false);
SELECT setval(pg_get_serial_sequence('user_account',        'id'), COALESCE((SELECT MAX(id) FROM user_account),        0) + 1, false);
SELECT setval(pg_get_serial_sequence('department',          'id'), COALESCE((SELECT MAX(id) FROM department),          0) + 1, false);
SELECT setval(pg_get_serial_sequence('subject',             'id'), COALESCE((SELECT MAX(id) FROM subject),             0) + 1, false);
SELECT setval(pg_get_serial_sequence('class',               'id'), COALESCE((SELECT MAX(id) FROM class),               0) + 1, false);
SELECT setval(pg_get_serial_sequence('staff',               'id'), COALESCE((SELECT MAX(id) FROM staff),               0) + 1, false);
SELECT setval(pg_get_serial_sequence('timetable',           'id'), COALESCE((SELECT MAX(id) FROM timetable),           0) + 1, false);
SELECT setval(pg_get_serial_sequence('timetable_template',  'id'), COALESCE((SELECT MAX(id) FROM timetable_template),  0) + 1, false);
SELECT setval(pg_get_serial_sequence('attendance',          'id'), COALESCE((SELECT MAX(id) FROM attendance),          0) + 1, false);
SELECT setval(pg_get_serial_sequence('lesson_plan',         'id'), COALESCE((SELECT MAX(id) FROM lesson_plan),         0) + 1, false);
SELECT setval(pg_get_serial_sequence('alteration',          'id'), COALESCE((SELECT MAX(id) FROM alteration),          0) + 1, false);
SELECT setval(pg_get_serial_sequence('alteration_audit',    'id'), COALESCE((SELECT MAX(id) FROM alteration_audit),    0) + 1, false);
SELECT setval(pg_get_serial_sequence('notification',        'id'), COALESCE((SELECT MAX(id) FROM notification),        0) + 1, false);
SELECT setval(pg_get_serial_sequence('workload_summary',    'id'), COALESCE((SELECT MAX(id) FROM workload_summary),    0) + 1, false);
