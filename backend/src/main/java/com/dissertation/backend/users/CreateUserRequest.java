package com.dissertation.backend.users;

public record CreateUserRequest(String fullName, String email, String password) {
}
