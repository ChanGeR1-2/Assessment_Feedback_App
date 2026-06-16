package com.dissertation.backend.app_users.dto;

import com.dissertation.backend.app_users.UserRole;

public record UserResponse(Long id, String fullName, String email, UserRole role) {
}
