package com.dissertation.backend.assessments.dto;

public record MarkingItemResponse(
        Long id,
        Long assessmentId,
        String name,
        Short maxMark,
        Short position
) {
}
