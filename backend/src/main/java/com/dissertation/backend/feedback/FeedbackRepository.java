package com.dissertation.backend.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT DISTINCT f FROM Feedback f " +
            "JOIN FETCH f.items i " +
            "JOIN FETCH i.markingItem " +
            "JOIN FETCH f.student " +
            "JOIN FETCH f.assessment a " +
            "JOIN FETCH a.module " +
            "LEFT JOIN FETCH f.lecturer " +
            "WHERE f.id = :id")
    Optional<Feedback> findByIdWithDetails(@Param("id") Long id);
    @Query("SELECT DISTINCT f FROM Feedback f " +
            "JOIN FETCH f.items i " +
            "JOIN FETCH i.markingItem " +
            "JOIN FETCH f.student " +
            "JOIN FETCH f.assessment a " +
            "JOIN FETCH a.module " +
            "LEFT JOIN FETCH f.lecturer " +
            "WHERE f.student.id = :studentId")
    List<Feedback> findByStudentId(@Param("studentId") Long studentId);
    @Query("SELECT DISTINCT f FROM Feedback f " +
            "JOIN FETCH f.items i " +
            "JOIN FETCH i.markingItem " +
            "JOIN FETCH f.student " +
            "JOIN FETCH f.assessment a " +
            "JOIN FETCH a.module " +
            "LEFT JOIN FETCH f.lecturer " +
            "WHERE f.assessment.id = :assessmentId")
    List<Feedback> findByAssessmentId(@Param("assessmentId") Long assessmentId);
    @Query("SELECT DISTINCT f FROM Feedback f " +
            "JOIN FETCH f.items i " +
            "JOIN FETCH i.markingItem " +
            "JOIN FETCH f.student " +
            "JOIN FETCH f.assessment a " +
            "JOIN FETCH a.module " +
            "LEFT JOIN FETCH f.lecturer " +
            "WHERE f.assessment.id = :assessmentId AND f.student.id = :studentId")
    Optional<Feedback> findByAssessmentIdAndStudentId(@Param("assessmentId") Long assessmentId, @Param("studentId") Long studentId);
    boolean existsByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
    boolean existsByAssessmentId(Long assessmentId);
}