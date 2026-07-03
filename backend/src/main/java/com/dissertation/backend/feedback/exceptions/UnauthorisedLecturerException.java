package com.dissertation.backend.feedback.exceptions;

public class UnauthorisedLecturerException extends RuntimeException {
    public UnauthorisedLecturerException(Long lecturerId, Long moduleId) {
        super(String.format("Lecturer %d is not authorised to give feedback for module %d", lecturerId, moduleId));
    }
}
