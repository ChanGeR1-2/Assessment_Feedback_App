package com.dissertation.backend.feedback.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FeedbackResponse(
        Long id,
        String assessmentTitle,
        Long assessmentId,
        String studentFullName,
        Long studentId,
        String lecturerFullName,
        Long lecturerId,
        Short mark,
        Integer totalMark,
        String summary,
        LocalDateTime createdAt,
        List<FeedbackItemResponse> items
) {
}
