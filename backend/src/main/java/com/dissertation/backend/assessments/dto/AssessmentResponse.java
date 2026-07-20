package com.dissertation.backend.assessments.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AssessmentResponse(
        Long id,
        String title,
        LocalDateTime dueDate,
        Long moduleId,
        List<MarkingItemResponse> markingItems,
        Integer totalMark
) {
}
