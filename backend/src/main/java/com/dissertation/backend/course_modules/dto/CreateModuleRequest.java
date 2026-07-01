package com.dissertation.backend.course_modules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateModuleRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Code is required")
        String code,
        @NotBlank(message = "Academic year is required")
        @Pattern(regexp = "^[0-9]{4}/[0-9]{4}$", message = "Academic year must be in the format YYYY/YYYY")
        String academicYear,
        Long lecturerId
) {
}
