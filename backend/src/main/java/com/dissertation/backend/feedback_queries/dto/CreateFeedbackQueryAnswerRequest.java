package com.dissertation.backend.feedback_queries.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFeedbackQueryAnswerRequest(
        @NotBlank(message = "Answer cannot be empty")
        @Size(max = 2000, message = "Answer is too long")
        String answer
) {
}
