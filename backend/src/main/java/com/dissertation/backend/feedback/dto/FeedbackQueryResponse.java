package com.dissertation.backend.feedback.dto;

import java.time.LocalDateTime;

public record FeedbackQueryResponse(
        Long id,
        Long feedbackId,
        String query,
        String studentFullName,
        LocalDateTime createdAt,
        FeedbackQueryAnswerResponse answer
) {
}
