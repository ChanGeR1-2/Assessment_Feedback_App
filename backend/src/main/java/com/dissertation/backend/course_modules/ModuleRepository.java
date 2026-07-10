package com.dissertation.backend.course_modules;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<CourseModule, Long> {
    boolean existsByCodeAndAcademicYear(String code, String academicYear);
    List<CourseModule> findByLecturerId(Long lecturerId);
}
