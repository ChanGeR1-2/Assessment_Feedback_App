package com.dissertation.backend.app_users.exceptions;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String email) {
        super(String.format("User with email %s already exists", email));
    }
}
