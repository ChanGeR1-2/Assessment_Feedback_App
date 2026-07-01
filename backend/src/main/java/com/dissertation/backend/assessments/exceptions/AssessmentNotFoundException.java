package com.dissertation.backend.assessments.exceptions;

public class AssessmentNotFoundException extends RuntimeException {
    public AssessmentNotFoundException() {
        super("Assessment not found");
    }
}
