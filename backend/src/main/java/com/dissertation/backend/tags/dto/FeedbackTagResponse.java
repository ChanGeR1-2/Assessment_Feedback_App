package com.dissertation.backend.tags.dto;

import com.dissertation.backend.tags.TagType;

public record FeedbackTagResponse(
        Long id,
        Long tagId,
        String tagName,
        TagType tagType
) {
}
