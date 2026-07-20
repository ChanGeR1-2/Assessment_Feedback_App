package com.dissertation.backend.assessments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    @Query("SELECT DISTINCT a FROM Assessment a LEFT JOIN FETCH a.markingItems WHERE a.module.id = :moduleId")
    List<Assessment> findByModuleId(@Param("moduleId") Long moduleId);
}
