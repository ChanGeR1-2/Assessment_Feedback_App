package com.dissertation.backend.enrolment.dto;

import jakarta.validation.constraints.NotNull;

public record CreateEnrolmentRequest(
        @NotNull(message = "Module ID is required")
        Long moduleId,
        @NotNull(message = "Student ID is required")
        Long studentId
) {
}
