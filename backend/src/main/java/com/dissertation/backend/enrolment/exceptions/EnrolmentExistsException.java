package com.dissertation.backend.enrolment.exceptions;

public class EnrolmentExistsException extends RuntimeException {
    public EnrolmentExistsException(Long studentId, Long moduleId) {
        super(String.format("Enrolment for student %d and module %d already exists", studentId, moduleId));
    }
}
