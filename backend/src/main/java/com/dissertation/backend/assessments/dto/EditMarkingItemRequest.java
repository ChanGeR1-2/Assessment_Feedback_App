package com.dissertation.backend.assessments.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EditMarkingItemRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotNull(message = "Max mark is required")
        @Min(value = 0, message = "Max mark must be at least 0")
        Short maxMark
) {
}
