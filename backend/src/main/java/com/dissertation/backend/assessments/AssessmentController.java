package com.dissertation.backend.assessments;

import com.dissertation.backend.assessments.dto.AssessmentResponse;
import com.dissertation.backend.assessments.dto.CreateAssessmentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AssessmentController {
    private final AssessmentService assessmentService;
    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/api/assessments")
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/api/assessments/{id}")
    public ResponseEntity<AssessmentResponse> getAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessment(id));
    }

    @GetMapping("/api/modules/{moduleId}/assessments")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByModuleId(@PathVariable Long moduleId) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByModuleId(moduleId));
    }

    @PostMapping("/api/assessments")
    public ResponseEntity<AssessmentResponse> createAssessment(@Valid @RequestBody CreateAssessmentRequest request) {
        return new ResponseEntity<>(assessmentService.createAssessment(request), HttpStatus.CREATED);
    }
}
