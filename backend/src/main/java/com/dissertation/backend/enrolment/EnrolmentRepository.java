package com.dissertation.backend.enrolment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrolmentRepository extends JpaRepository<Enrolment, Long> {
    boolean existsByStudentIdAndModuleId(Long studentId, Long moduleId);
    @Query("SELECT e FROM Enrolment e " +
            "JOIN FETCH e.module m " +
            "LEFT JOIN FETCH m.lecturer " +
            "WHERE e.student.id = :studentId")
    List<Enrolment> findByStudentId(@Param("studentId") Long studentId);
    @Query("SELECT e FROM Enrolment e JOIN FETCH e.student WHERE e.module.id = :moduleId")
    List<Enrolment> findByModuleId(@Param("moduleId") Long moduleId);
    @Query("SELECT e FROM Enrolment e " +
            "JOIN FETCH e.student " +
            "JOIN FETCH e.module m " +
            "JOIN FETCH m.lecturer l " +
            "WHERE l.id = :lecturerId " +
            "AND (:moduleId IS NULL OR m.id = :moduleId)")
    List<Enrolment> findByLecturerId(@Param("lecturerId") Long lecturerId,
                                     @Param("moduleId") Long moduleId);
}
