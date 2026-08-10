package com.dissertation.backend.assessments.exceptions;

public class MarkingItemNotForAssessmentException extends RuntimeException {
    public MarkingItemNotForAssessmentException(Long markingItemId, Long assessmentId) {
        super(String.format("Marking item with id %d is not for assessment with id %d", markingItemId, assessmentId));
    }
}
