package com.dissertation.backend.assessments.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAssessmentRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotNull(message = "Due date is required")
        LocalDateTime dueDate,
        @NotNull(message = "Module ID is required")
        Long moduleId,
        @NotNull(message = "Weight is required")
        @Min(value = 0, message = "Weight must be at least 0")
        Short weight
) {
}
