package com.dissertation.backend.course_modules.exceptions;

public class ModuleExistsException extends RuntimeException {
    public ModuleExistsException(String code, String academicYear) {
        super(String.format("Module with code %s and academic year %s already exists", code, academicYear));
    }
}
