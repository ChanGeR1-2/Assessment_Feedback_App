package com.dissertation.backend.feedback.exceptions;

public class DuplicateMarkingItemException extends RuntimeException {
    public DuplicateMarkingItemException(String message) {
        super(message);
    }
}
