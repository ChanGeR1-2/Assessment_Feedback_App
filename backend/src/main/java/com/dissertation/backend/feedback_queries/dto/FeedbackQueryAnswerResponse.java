package com.dissertation.backend.feedback_queries.dto;

import java.time.LocalDateTime;

public record FeedbackQueryAnswerResponse(
        Long id,
        String answer,
        String lecturerFullName,
        LocalDateTime createdAt
) {
}
