package com.dissertation.backend.assessments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findAssessmentById(Long id);
    List<Assessment> findByModuleId(Long moduleId);
}
