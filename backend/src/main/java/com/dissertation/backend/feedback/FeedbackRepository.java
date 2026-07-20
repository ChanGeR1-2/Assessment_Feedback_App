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
            "JOIN FETCH f.assessment " +
            "JOIN FETCH f.lecturer " +
            "WHERE f.id = :id")
    Optional<Feedback> findByIdWithDetails(@Param("id") Long id);
    boolean existsByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
    List<Feedback> findByStudentId(Long studentId);
    List<Feedback> findByAssessmentId(Long assessmentId);
    boolean existsByAssessmentId(Long assessmentId);
    Optional<Feedback> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
}
