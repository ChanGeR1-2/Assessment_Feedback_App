package com.dissertation.backend.feedback.exceptions;

public class MarkingItemNotForAssessmentException extends RuntimeException {
    public MarkingItemNotForAssessmentException(Long markingItemId, Long assessmentId) {
        super(String.format("Marking item with id %d is not for assessment %d", markingItemId, assessmentId));
    }
}
