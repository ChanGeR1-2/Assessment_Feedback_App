package com.dissertation.backend.assessments.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MarkingItemReorderRequest(
        @NotEmpty List<@NotNull Long> orderedIds
) {
}
