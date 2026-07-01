package com.dissertation.backend.enrolment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrolmentRepository extends JpaRepository<Enrolment, Long> {
    boolean existsByStudentIdAndModuleId(Long studentId, Long moduleId);

    List<Enrolment> findByStudentId(Long studentId);

    List<Enrolment> findByModuleId(Long moduleId);
}
