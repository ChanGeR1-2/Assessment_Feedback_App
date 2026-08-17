package com.dissertation.backend.feedback_audio.exceptions;

import java.io.IOException;

public class AudioStorageException extends RuntimeException {
    public AudioStorageException(String message, IOException e) {
        super(String.format("Error storing audio: %s", message), e);
    }
}
