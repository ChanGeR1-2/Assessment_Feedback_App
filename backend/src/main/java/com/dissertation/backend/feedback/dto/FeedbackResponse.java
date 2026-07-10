package com.dissertation.backend.feedback.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        String assessmentTitle,
        Long assessmentId,
        String studentFullName,
        Long studentId,
        Long lecturerId,
        Integer mark,
        String strengths,
        String improvements,
        String actions,
        LocalDateTime createdAt
) {
}
