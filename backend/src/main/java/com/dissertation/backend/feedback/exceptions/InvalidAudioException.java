package com.dissertation.backend.feedback.exceptions;

public class InvalidAudioException extends RuntimeException {
    public InvalidAudioException(String message) {
        super(String.format("Invalid audio: %s", message));
    }
}
