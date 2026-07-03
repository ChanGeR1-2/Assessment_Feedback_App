package com.dissertation.backend.enrolment.dto;

import java.time.LocalDateTime;

public record EnrolmentResponse(
        Long moduleId,
        Long studentId,
        Long id,
        LocalDateTime enrolmentDate
) {
}
