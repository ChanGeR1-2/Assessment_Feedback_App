package com.dissertation.backend.feedback.exceptions;

public class FeedbackExistsException extends RuntimeException {
    public FeedbackExistsException(Long studentId, Long assessmentId) {
        super(String.format("Feedback for student %d and assessment %d already exists", studentId, assessmentId));
    }
}
