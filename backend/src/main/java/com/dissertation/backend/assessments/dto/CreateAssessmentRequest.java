package com.dissertation.backend.assessments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAssessmentRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotNull(message = "Due date is required")
        LocalDateTime dueDate,
        @NotNull(message = "Module ID is required")
        Long moduleId
) {
}
