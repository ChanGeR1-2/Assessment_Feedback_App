package com.dissertation.backend.assessments.exceptions;

public class RubricLockedException extends RuntimeException {
    public RubricLockedException(Long assessmentId) {
        super(String.format("Rubric for assessment with id %d is locked", assessmentId));
    }
}
