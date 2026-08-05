package com.dissertation.backend.feedback_queries.exceptions;

public class FeedbackQueryNotFoundException extends RuntimeException {
    public FeedbackQueryNotFoundException(Long id) {
        super(String.format("Feedback query with id %d not found", id));
    }
}
