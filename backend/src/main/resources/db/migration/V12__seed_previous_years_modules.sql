INSERT INTO course_module (title, code, academic_year, lecturer_id)
VALUES
    ('Programming Fundamentals',      'CS100', '2024/2025', NULL),
    ('Discrete Mathematics',          'CS110', '2024/2025', NULL),
    ('Data Structures and Algorithms','CS200', '2025/2026', NULL),
    ('Web Development',               'CS210', '2025/2026', NULL)
ON CONFLICT (code, academic_year) DO NOTHING;

INSERT INTO assessment (title, due_date, module_id)
SELECT v.title, v.due_date, m.id
FROM (VALUES
          -- 2024/2025
          ('AS1', TIMESTAMP '2024-10-18 23:59:00', 'CS100', '2024/2025'),
          ('AS2', TIMESTAMP '2024-12-13 23:59:00', 'CS100', '2024/2025'),
          ('AS3', TIMESTAMP '2025-02-21 23:59:00', 'CS100', '2024/2025'),
          ('AS1', TIMESTAMP '2025-01-17 23:59:00', 'CS110', '2024/2025'),
          ('AS2', TIMESTAMP '2025-03-14 23:59:00', 'CS110', '2024/2025'),
          ('AS3', TIMESTAMP '2025-05-16 23:59:00', 'CS110', '2024/2025'),
          -- 2025/2026
          ('AS1', TIMESTAMP '2025-10-17 23:59:00', 'CS200', '2025/2026'),
          ('AS2', TIMESTAMP '2025-12-12 23:59:00', 'CS200', '2025/2026'),
          ('AS3', TIMESTAMP '2026-02-20 23:59:00', 'CS200', '2025/2026'),
          ('AS1', TIMESTAMP '2026-01-16 23:59:00', 'CS210', '2025/2026'),
          ('AS2', TIMESTAMP '2026-03-13 23:59:00', 'CS210', '2025/2026'),
          ('AS3', TIMESTAMP '2026-05-15 23:59:00', 'CS210', '2025/2026')
     ) AS v(title, due_date, code, year)
         JOIN course_module m ON m.code = v.code AND m.academic_year = v.year;

UPDATE assessment a
SET weight = v.weight
FROM (VALUES
          ('AS1', 'CS100', '2024/2025', 30), ('AS2', 'CS100', '2024/2025', 30), ('AS3', 'CS100', '2024/2025', 40),
          ('AS1', 'CS110', '2024/2025', 25), ('AS2', 'CS110', '2024/2025', 35), ('AS3', 'CS110', '2024/2025', 40),
          ('AS1', 'CS200', '2025/2026', 30), ('AS2', 'CS200', '2025/2026', 30), ('AS3', 'CS200', '2025/2026', 40),
          ('AS1', 'CS210', '2025/2026', 25), ('AS2', 'CS210', '2025/2026', 35), ('AS3', 'CS210', '2025/2026', 40)
     ) AS v(title, code, year, weight)
         JOIN course_module m ON m.code = v.code AND m.academic_year = v.year
WHERE a.title = v.title AND a.module_id = m.id;