package com.dissertation.backend.assessments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    @Query("SELECT DISTINCT a FROM Assessment a LEFT JOIN FETCH a.markingItems WHERE a.module.id = :moduleId")
    List<Assessment> findByModuleId(@Param("moduleId") Long moduleId);
    @Query("SELECT DISTINCT a FROM Assessment a " +
            "LEFT JOIN FETCH a.markingItems " +
            "WHERE a.module.lecturer.id = :lecturerId")
    List<Assessment> findByLecturerId(@Param("lecturerId") Long lecturerId);
    @Query("SELECT DISTINCT a FROM Assessment a " +
            "LEFT JOIN FETCH a.markingItems " +
            "JOIN Enrolment e ON e.module = a.module " +
            "WHERE e.student.id = :studentId")
    List<Assessment> findByStudentId(@Param("studentId") Long studentId);
    boolean existsByIdAndModule_Lecturer_Id(Long assessmentId, Long lecturerId);
}
