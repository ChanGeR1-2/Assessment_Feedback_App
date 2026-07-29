package com.dissertation.backend.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackQueryRepository extends JpaRepository<FeedbackQuery, Long> {

    @Query("SELECT q FROM FeedbackQuery q " +
            "JOIN FETCH q.feedback f " +
            "JOIN FETCH f.assessment " +
            "JOIN FETCH q.student " +
            "WHERE f.assessment.module.lecturer.id = :lecturerId " +
            "AND NOT EXISTS (SELECT r FROM FeedbackQueryAnswer r WHERE r.feedbackQuery = q)")
    List<FeedbackQuery> findUnansweredByLecturerId(@Param("lecturerId") Long lecturerId);

    @Query("SELECT q FROM FeedbackQuery q " +
            "JOIN FETCH q.feedback f " +
            "JOIN FETCH f.assessment " +
            "WHERE q.student.id = :studentId")
    List<FeedbackQuery> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT q FROM FeedbackQuery q " +
            "JOIN FETCH q.feedback f " +
            "JOIN FETCH f.assessment " +
            "WHERE q.id = :id")
    Optional<FeedbackQuery> findByIdWithDetails(@Param("id") Long id);

    Optional<FeedbackQuery> findByFeedbackId(Long feedbackId);

    boolean existsByFeedbackId(Long feedbackId);
}