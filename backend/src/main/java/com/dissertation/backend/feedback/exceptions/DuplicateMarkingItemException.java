package com.dissertation.backend.feedback.exceptions;

public class DuplicateMarkingItemException extends RuntimeException {
    public DuplicateMarkingItemException(Long markingItemId) {
        super(String.format("Marking item with id %d has already been assigned", markingItemId));
    }
}
