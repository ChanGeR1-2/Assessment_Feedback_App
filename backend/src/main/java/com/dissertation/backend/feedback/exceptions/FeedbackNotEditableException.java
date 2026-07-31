package com.dissertation.backend.feedback.exceptions;

public class FeedbackNotEditableException extends RuntimeException {
    public FeedbackNotEditableException(Long feedbackId) {
        super(String.format("Feedback with id %d is not editable", feedbackId));
    }
}
