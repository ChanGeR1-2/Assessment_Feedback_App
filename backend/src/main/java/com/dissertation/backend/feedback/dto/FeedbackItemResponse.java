package com.dissertation.backend.feedback.dto;

public record FeedbackItemResponse(
        Long id,
        Long feedbackId,
        Long markingItemId,
        String markingItemName,
        Short awardedMark,
        Short maxMark,
        String comment
) {
}
