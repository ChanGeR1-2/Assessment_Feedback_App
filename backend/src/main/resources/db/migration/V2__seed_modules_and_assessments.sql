INSERT INTO course_module (title, code, academic_year, lecturer_id)
VALUES
    ('Introduction to Databases', 'CS101', '2026/2027', NULL),
    ('Software Engineering',      'CS202', '2026/2027', NULL)
ON CONFLICT (code, academic_year) DO NOTHING;

INSERT INTO assessment (title, due_date, module_id)
SELECT v.title, v.due_date, m.id
FROM (VALUES
          ('AS1', TIMESTAMP '2026-10-15 23:59:00', 'CS101'),
          ('AS2', TIMESTAMP '2026-12-15 23:59:00', 'CS101'),
          ('AS3', TIMESTAMP '2027-02-15 23:59:00', 'CS101'),
          ('AS1', TIMESTAMP '2027-01-12 23:59:00', 'CS202'),
          ('AS1', TIMESTAMP '2027-03-18 23:59:00', 'CS202'),
          ('AS1', TIMESTAMP '2027-05-27 23:59:00', 'CS202')
     ) AS v(title, due_date, code)
         JOIN course_module m ON m.code = v.code AND m.academic_year = '2026/2027';
