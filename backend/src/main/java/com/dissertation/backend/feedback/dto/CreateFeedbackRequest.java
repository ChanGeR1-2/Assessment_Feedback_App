package com.dissertation.backend.feedback.dto;

import com.dissertation.backend.tags.dto.CreateFeedbackTagRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateFeedbackRequest(
        @NotNull(message = "Assessment ID is required")
        Long assessmentId,
        @NotNull(message = "Student ID is required")
        Long studentId,
        @NotBlank(message = "Summary is required")
        String summary,
        @NotEmpty(message = "At least one feedback item is required")
        @Size(max = 50, message = "Too many feedback items")
        @Valid
        List<CreateFeedbackItemRequest> items,
        @Valid
        List<CreateFeedbackTagRequest> tags
) {
}
