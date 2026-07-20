package com.dissertation.backend.assessments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarkingItemRepository extends JpaRepository<MarkingItem, Long> {
    List<MarkingItem> findByAssessmentIdOrderByPosition(Long assessmentId);
}
