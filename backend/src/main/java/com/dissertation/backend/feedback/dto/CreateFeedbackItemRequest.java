package com.dissertation.backend.feedback.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFeedbackItemRequest(
        @NotNull(message = "Marking item ID is required")
        Long markingItemId,

        @NotNull(message = "Mark is required")
        @Min(value = 0, message = "Mark cannot be negative")
        Short awardedMark,

        @Size(max = 5000, message = "Comment too long")
        String comment
) {
}
