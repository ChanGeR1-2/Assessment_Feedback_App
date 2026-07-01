package com.dissertation.backend.assessments.exceptions;

public class InvalidModuleException extends RuntimeException {
    public InvalidModuleException(Long moduleId) {
        super(String.format("Module with id %d not found", moduleId));
    }
}
