package com.dissertation.backend.feedback.dto;

import java.util.List;

public record PublishFeedbackListRequest(
        List<Long> studentIds
) {
}
