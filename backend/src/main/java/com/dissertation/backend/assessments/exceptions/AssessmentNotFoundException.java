package com.dissertation.backend.assessments.exceptions;

public class AssessmentNotFoundException extends RuntimeException {
    public AssessmentNotFoundException(Long id) {
        super(String.format("Assessment with id %d not found", id));
    }
}
