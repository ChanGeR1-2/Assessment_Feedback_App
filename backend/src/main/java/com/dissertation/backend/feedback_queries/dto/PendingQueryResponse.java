package com.dissertation.backend.feedback_queries.dto;

import java.time.LocalDateTime;

public record PendingQueryResponse(
        Long id,
        Long feedbackId,
        String query,
        String studentFullName,
        String assessmentTitle,
        LocalDateTime createdAt
) {
}
