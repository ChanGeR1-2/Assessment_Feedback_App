package com.dissertation.backend.assessments.exceptions;

public class MarkingItemNotFoundException extends RuntimeException {
    public MarkingItemNotFoundException(Long markingItemId) {
        super(String.format("Marking item with id %d not found", markingItemId));
    }
}
