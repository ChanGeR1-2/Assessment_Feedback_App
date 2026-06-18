package com.dissertation.backend.auth.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Invalid Password");
    }
}
