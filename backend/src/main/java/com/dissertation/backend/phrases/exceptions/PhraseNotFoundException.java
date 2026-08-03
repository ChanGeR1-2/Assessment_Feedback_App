package com.dissertation.backend.phrases.exceptions;

public class PhraseNotFoundException extends RuntimeException {
    public PhraseNotFoundException(Long id) {
        super(String.format("Phrase with id %d not found", id));
    }
}
