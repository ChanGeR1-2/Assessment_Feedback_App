package com.dissertation.backend.feedback;

import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FeedbackController {
    private final FeedbackService feedbackService;
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }


    @PostMapping("/api/feedback")
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request, @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(feedbackService.saveFeedback(request, principal.getId()), HttpStatus.CREATED);
    }


    @GetMapping("/api/assessments/{assessmentId}/students/{studentId}/feedback")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long assessmentId,
                                                        @PathVariable Long studentId,
                                                        @AuthenticationPrincipal AppUserDetails principal) {
        return feedbackService.getFeedbackByAssessmentIdAndStudentId(studentId, assessmentId, principal)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/api/assessments/{assessmentId}/feedback")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByAssessmentId(@PathVariable Long assessmentId, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(feedbackService.getFeedbackByAssessmentId(assessmentId, principal));
    }


    @GetMapping(path = "/api/students/{studentId}/feedback")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByStudentId(
            @PathVariable Long studentId, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(feedbackService.getFeedbackByStudentId(studentId, principal));
    }


    @GetMapping(path = "/api/feedback/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(feedbackService.getFeedbackById(id, principal));
    }
}
