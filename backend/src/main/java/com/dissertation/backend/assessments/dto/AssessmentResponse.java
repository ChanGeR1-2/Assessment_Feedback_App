package com.dissertation.backend.assessments.dto;

import java.time.LocalDateTime;

public record AssessmentResponse(
        Long id,
        String title,
        LocalDateTime dueDate,
        Long moduleId
) {
}
