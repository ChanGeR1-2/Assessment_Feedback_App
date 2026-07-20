package com.dissertation.backend.assessments;

import com.dissertation.backend.assessments.dto.*;
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

    @PostMapping("/api/assessments/{id}/marking-items")
    public ResponseEntity<MarkingItemResponse> createMarkingItem(@PathVariable Long id, @Valid @RequestBody CreateMarkingItemRequest request) {
        return new ResponseEntity<>(assessmentService.createMarkingItem(id, request), HttpStatus.CREATED);
    }

    @PatchMapping("/api/assessments/{assessmentId}/marking-items/{id}")
    public ResponseEntity<MarkingItemResponse> editMarkingItem(@PathVariable Long id, @PathVariable Long assessmentId, @Valid @RequestBody EditMarkingItemRequest request) {
        return ResponseEntity.ok(assessmentService.editMarkingItem(assessmentId, id, request));
    }

    @DeleteMapping("/api/assessments/{assessmentId}/marking-items/{id}")
    public ResponseEntity<Void> deleteMarkingItem(@PathVariable Long assessmentId, @PathVariable Long id) {
        assessmentService.deleteMarkingItem(assessmentId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/assessments/{assessmentId}/marking-items/order")
    public ResponseEntity<Void> reorderMarkingItems(@PathVariable Long assessmentId, @Valid @RequestBody MarkingItemReorderRequest request) {
        assessmentService.reorderMarkingItems(assessmentId, request.orderedIds());
        return ResponseEntity.noContent().build();
    }
}
