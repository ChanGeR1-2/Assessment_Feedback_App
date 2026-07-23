package com.dissertation.backend.feedback;

import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FeedbackController {
    private final FeedbackService feedbackService;
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // Lecturer id is passed as a request param until security is implemented
    // TODO: SECURITY
    @PostMapping("/api/feedback")
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request, @RequestParam Long lecturerId) {
        return new ResponseEntity<>(feedbackService.saveFeedback(request, lecturerId), HttpStatus.CREATED);
    }

    // TODO: SECURITY
    @GetMapping("/api/assessments/{assessmentId}/students/{studentId}/feedback")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long assessmentId,
                                                        @PathVariable Long studentId) {
        return feedbackService.getFeedbackByAssessmentIdAndStudentId(studentId, assessmentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/api/assessments/{assessmentId}/feedback")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByAssessmentId(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByAssessmentId(assessmentId));
    }

    // TODO: SECURITY
    @GetMapping(path = "/api/students/{studentId}/feedback")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByStudentId(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByStudentId(studentId));
    }

    // TODO: SECURITY
    @GetMapping(path = "/api/feedback/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getFeedbackById(id));
    }
}
