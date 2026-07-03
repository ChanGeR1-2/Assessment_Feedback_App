package com.dissertation.backend.course_modules.exceptions;

public class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException(Long id) {
        super(String.format("Module with id %d not found", id));
    }
}
