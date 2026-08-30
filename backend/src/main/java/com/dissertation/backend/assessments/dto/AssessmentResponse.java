package com.dissertation.backend.assessments.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AssessmentResponse(
        Long id,
        String title,
        LocalDateTime dueDate,
        Long moduleId,
        String moduleTitle,
        List<MarkingItemResponse> markingItems,
        Integer totalMark,
        Short weight,
        String academicYear,
        LocalDateTime feedbackDueDate,
        Boolean isRubricLocked
) {
}
