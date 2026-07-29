package com.dissertation.backend.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFeedbackQueryRequest(
        @NotBlank(message = "Query cannot be empty")
        @Size(max = 2000, message = "Query is too long")
        String query
) {
}
