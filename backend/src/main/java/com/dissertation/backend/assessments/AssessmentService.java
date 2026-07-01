package com.dissertation.backend.assessments;

import com.dissertation.backend.assessments.dto.AssessmentResponse;
import com.dissertation.backend.assessments.dto.CreateAssessmentRequest;
import com.dissertation.backend.assessments.exceptions.AssessmentNotFoundException;
import com.dissertation.backend.assessments.exceptions.InvalidModuleException;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssessmentService {
    private final AssessmentRepository assessmentRepository;
    private final ModuleRepository moduleRepository;
    public AssessmentService(AssessmentRepository assessmentRepository, ModuleRepository moduleRepository) {
        this.assessmentRepository = assessmentRepository;
        this.moduleRepository = moduleRepository;
    }

    public List<AssessmentResponse> getAllAssessments() {
        return assessmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AssessmentResponse getAssessment(Long id) {
        return assessmentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(AssessmentNotFoundException::new);
    }

    public AssessmentResponse createAssessment(CreateAssessmentRequest request) {
        CourseModule module = moduleRepository.findById(request.moduleId())
                .orElseThrow(() -> new InvalidModuleException(request.moduleId()));

        Assessment assessment = new Assessment();
        assessment.setTitle(request.title());
        assessment.setDueDate(request.dueDate());
        assessment.setModule(module);
        return toResponse(assessmentRepository.save(assessment));
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        return new AssessmentResponse(assessment.getTitle(), assessment.getDueDate(), assessment.getModule().getId());
    }
}
