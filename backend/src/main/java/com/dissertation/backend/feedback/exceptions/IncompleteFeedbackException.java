package com.dissertation.backend.feedback.exceptions;

public class IncompleteFeedbackException extends RuntimeException {
    public IncompleteFeedbackException(String message) {
        super(message);
    }
}
