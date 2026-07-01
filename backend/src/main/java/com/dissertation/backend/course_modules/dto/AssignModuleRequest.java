package com.dissertation.backend.course_modules.dto;

import jakarta.validation.constraints.NotNull;

public record AssignModuleRequest(
        @NotNull(message = "Lecturer ID is required")
        Long lecturerId
) {
}
