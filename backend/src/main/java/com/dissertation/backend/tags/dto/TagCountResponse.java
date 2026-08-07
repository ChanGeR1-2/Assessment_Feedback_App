package com.dissertation.backend.tags.dto;

import com.dissertation.backend.tags.TagType;

public record TagCountResponse(
        String tagName,
        TagType tagType,
        Long count
) {
}
