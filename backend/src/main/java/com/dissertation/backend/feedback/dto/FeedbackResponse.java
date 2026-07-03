package com.dissertation.backend.feedback.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Long assessmentId,
        Long studentId,
        Long lecturerId,
        Integer mark,
        String strengths,
        String improvements,
        String actions,
        LocalDateTime createdAt
) {
}
