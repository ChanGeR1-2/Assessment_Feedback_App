package com.dissertation.backend.tags.exceptions;

public class DuplicateTagException extends RuntimeException {
    public DuplicateTagException(String message) {
        super(message);
    }
}
