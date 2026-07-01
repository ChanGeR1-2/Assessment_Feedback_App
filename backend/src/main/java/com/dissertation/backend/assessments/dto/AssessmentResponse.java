package com.dissertation.backend.assessments.dto;

import java.time.LocalDateTime;

public record AssessmentResponse(
        String title,
        LocalDateTime dueDate,
        Long moduleId
) {
}
