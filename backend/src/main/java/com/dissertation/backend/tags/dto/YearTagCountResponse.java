package com.dissertation.backend.tags.dto;

import com.dissertation.backend.tags.TagType;

public record YearTagCountResponse(
        String academicYear,
        String name,
        TagType tagType,
        Long count
) {
}
