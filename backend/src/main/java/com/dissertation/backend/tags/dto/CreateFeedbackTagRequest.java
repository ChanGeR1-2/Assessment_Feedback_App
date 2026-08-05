package com.dissertation.backend.tags.dto;

import com.dissertation.backend.tags.TagType;
import jakarta.validation.constraints.NotNull;

public record CreateFeedbackTagRequest(
        @NotNull(message = "Tag ID is required")
        Long tagId,
        @NotNull(message = "Tag type is required")
        TagType tagType
) {
}
