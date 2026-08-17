package com.dissertation.backend.feedback_audio.exceptions;

public class InvalidAudioException extends RuntimeException {
    public InvalidAudioException(String message) {
        super(String.format("Invalid audio: %s", message));
    }
}
