package com.dissertation.backend.course_modules;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<CourseModule, Long> {
    boolean existsByCodeAndAcademicYear(String code, String academicYear);
}
