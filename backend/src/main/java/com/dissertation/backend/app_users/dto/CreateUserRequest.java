package com.dissertation.backend.app_users.dto;

import com.dissertation.backend.app_users.UserRole;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        @NotNull(message = "Role is required")
        UserRole role) {
}
