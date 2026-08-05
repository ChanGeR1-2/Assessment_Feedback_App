package com.dissertation.backend.feedback_queries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackQueryAnswerRepository extends JpaRepository<FeedbackQueryAnswer, Long> {
    Optional<FeedbackQueryAnswer> findByFeedbackQueryId(Long feedbackQueryId);
    boolean existsByFeedbackQueryId(Long feedbackQueryId);
}
