package com.dissertation.backend.feedback.exceptions;

public class StudentNotEnrolledException extends RuntimeException {
    public StudentNotEnrolledException(Long studentId, Long moduleId) {
        super(String.format("Student %d is not enrolled in module %d", studentId, moduleId));
    }
}
