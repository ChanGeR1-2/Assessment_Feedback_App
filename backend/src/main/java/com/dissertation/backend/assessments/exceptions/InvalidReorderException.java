package com.dissertation.backend.assessments.exceptions;

public class InvalidReorderException extends RuntimeException {
    public InvalidReorderException(Long assessmentId) {
        super(String.format("Invalid marking item reorder for assessment with id %d", assessmentId));
    }
}
