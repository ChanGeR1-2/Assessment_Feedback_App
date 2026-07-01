package com.dissertation.backend.course_modules.exceptions;

public class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException() {
        super("Module not found");
    }
}
