package com.dissertation.backend.feedback.exceptions;

public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException(Long id) {
        super(String.format("Feedback with id %d not found", id));
    }

    public FeedbackNotFoundException(Long studentId, Long assessmentId) {
        super(String.format("Feedback for student %d and assessment %d not found", studentId, assessmentId));
    }
}
