package com.dissertation.backend.feedback.exceptions;

public class AudioNotFoundException extends RuntimeException {
    public AudioNotFoundException(String filename) {
        super(String.format("Audio file %s not found", filename));
    }
    public AudioNotFoundException(Long id) {
        super(String.format("Audio with id %d not found", id));
    }
}
