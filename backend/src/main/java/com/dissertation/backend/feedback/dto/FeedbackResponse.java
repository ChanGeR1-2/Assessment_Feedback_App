package com.dissertation.backend.feedback.dto;

import com.dissertation.backend.feedback.FeedbackStatus;
import com.dissertation.backend.tags.dto.FeedbackTagResponse;

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
        List<FeedbackItemResponse> items,
        Long moduleId,
        String moduleTitle,
        String academicYear,
        FeedbackStatus status,
        List<FeedbackTagResponse> tags
) {
}
