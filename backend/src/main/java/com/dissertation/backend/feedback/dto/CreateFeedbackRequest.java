package com.dissertation.backend.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFeedbackRequest(
        @NotNull(message = "Assessment ID is required")
        Long assessmentId,
        @NotNull(message = "Student ID is required")
        Long studentId,
        @NotNull(message = "Mark is required")
        @Min(value = 0, message = "Mark must be at least 0")
        @Max(value = 100, message = "Mark must be less than or equal to 100")
        Short mark,
        @NotBlank(message = "Strengths are required")
        String strengths,
        @NotBlank(message = "Improvements are required")
        String improvements,
        @NotBlank(message = "Actions are required")
        String actions
) {
}
