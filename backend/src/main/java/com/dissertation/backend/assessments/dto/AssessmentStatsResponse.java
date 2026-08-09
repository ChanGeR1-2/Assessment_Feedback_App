package com.dissertation.backend.assessments.dto;

public record AssessmentStatsResponse(
        Long assessmentId,
        Long enrolled,
        Long drafts,
        Long published,
        Long todo
) {
}
