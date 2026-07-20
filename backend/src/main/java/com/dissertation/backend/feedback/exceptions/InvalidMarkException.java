package com.dissertation.backend.feedback.exceptions;

public class InvalidMarkException extends RuntimeException {
    public InvalidMarkException(Short mark, Short maxMark, Long markingItemId) {
        super(String.format("Mark %d is invalid for marking item %d (max mark is %d)", mark, markingItemId, maxMark));
    }
}
