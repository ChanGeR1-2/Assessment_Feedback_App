UPDATE assessment a
SET weight = v.weight
FROM (VALUES
          ('AS1', 'CS101', 30),
          ('AS2', 'CS101', 30),
          ('AS3', 'CS101', 40),
          ('AS1', 'CS202', 25),
          ('AS2', 'CS202', 35),
          ('AS3', 'CS202', 40)
     ) AS v(title, code, weight)
         JOIN course_module m ON m.code = v.code AND m.academic_year = '2026/2027'
WHERE a.title = v.title AND a.module_id = m.id;