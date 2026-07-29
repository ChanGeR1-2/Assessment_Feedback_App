package com.dissertation.backend.feedback.dto;

import java.time.LocalDateTime;

public record FeedbackQueryAnswerResponse(
        Long id,
        String answer,
        String lecturerFullName,
        LocalDateTime createdAt
) {
}
