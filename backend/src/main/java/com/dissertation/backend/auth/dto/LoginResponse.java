package com.dissertation.backend.auth.dto;

import com.dissertation.backend.app_users.UserRole;

public record LoginResponse(
        String token,
        Long id,
        String fullName,
        String email,
        UserRole role
) {
}
