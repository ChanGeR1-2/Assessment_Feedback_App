package com.dissertation.backend.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
    List<Feedback> findByStudentId(Long studentId);
    List<Feedback> findByAssessmentId(Long assessmentId);
    Optional<Feedback> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
}
