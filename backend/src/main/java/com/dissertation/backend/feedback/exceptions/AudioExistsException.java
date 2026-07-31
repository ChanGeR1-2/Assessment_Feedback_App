package com.dissertation.backend.feedback.exceptions;

public class AudioExistsException extends RuntimeException {
    public AudioExistsException(Long feedbackAudioId) {
        super(String.format("Audio with id %d already exists", feedbackAudioId));
    }
}
