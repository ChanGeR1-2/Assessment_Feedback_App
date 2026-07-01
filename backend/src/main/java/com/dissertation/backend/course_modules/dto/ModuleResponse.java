package com.dissertation.backend.course_modules.dto;

public record ModuleResponse(
        Long id,
        String title,
        String code,
        String academicYear,
        Long lecturerId
) {
}
