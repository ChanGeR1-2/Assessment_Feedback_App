package com.dissertation.backend.assessments;

import com.dissertation.backend.assessments.dto.*;
import com.dissertation.backend.config.AppUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AssessmentController {
    private final AssessmentService assessmentService;
    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/api/assessments")
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments(@AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(assessmentService.getAllAssessments(principal));
    }

    @GetMapping("/api/assessments/stats")
    public ResponseEntity<List<AssessmentStatsResponse>> getAssessmentStats(@AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(assessmentService.getAllAssessmentStatsByLecturer(principal));
    }

    @GetMapping("/api/assessments/{id}")
    public ResponseEntity<AssessmentResponse> getAssessment(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(assessmentService.getAssessment(id, principal));
    }

    @GetMapping("/api/modules/{moduleId}/assessments")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByModuleId(@PathVariable Long moduleId, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByModuleId(moduleId, principal));
    }

    @PostMapping("/api/assessments")
    public ResponseEntity<AssessmentResponse> createAssessment(@Valid @RequestBody CreateAssessmentRequest request, @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(assessmentService.createAssessment(request, principal), HttpStatus.CREATED);
    }

    @PostMapping("/api/assessments/{id}/marking-items")
    public ResponseEntity<MarkingItemResponse> createMarkingItem(@PathVariable Long id, @Valid @RequestBody CreateMarkingItemRequest request, @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(assessmentService.createMarkingItem(id, request, principal), HttpStatus.CREATED);
    }

    @PatchMapping("/api/assessments/{assessmentId}/marking-items/{id}")
    public ResponseEntity<MarkingItemResponse> editMarkingItem(@PathVariable Long id,
                                                               @PathVariable Long assessmentId,
                                                               @Valid @RequestBody EditMarkingItemRequest request,
                                                               @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(assessmentService.editMarkingItem(assessmentId, id, request, principal));
    }

    @DeleteMapping("/api/assessments/{assessmentId}/marking-items/{id}")
    public ResponseEntity<Void> deleteMarkingItem(@PathVariable Long assessmentId,
                                                  @PathVariable Long id,
                                                  @AuthenticationPrincipal AppUserDetails principal) {
        assessmentService.deleteMarkingItem(assessmentId, id, principal);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/assessments/{assessmentId}/marking-items/order")
    public ResponseEntity<Void> reorderMarkingItems(@PathVariable Long assessmentId,
                                                    @Valid @RequestBody MarkingItemReorderRequest request,
                                                    @AuthenticationPrincipal AppUserDetails principal) {
        assessmentService.reorderMarkingItems(assessmentId, request.orderedIds(), principal);
        return ResponseEntity.noContent().build();
    }
}
