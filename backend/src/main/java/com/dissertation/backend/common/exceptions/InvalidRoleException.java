package com.dissertation.backend.common.exceptions;

import com.dissertation.backend.app_users.UserRole;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(Long id, UserRole role) {
        super(String.format("User with id %d must have the role %s", id, role));
    }
}
