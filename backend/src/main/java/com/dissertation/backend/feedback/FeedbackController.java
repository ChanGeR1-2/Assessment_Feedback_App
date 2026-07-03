package com.dissertation.backend.feedback;

import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // Lecturer id is passed as a request param until security is implemented
    // TODO: SECURITY
    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request, @RequestParam Long lecturerId) {
        return new ResponseEntity<>(feedbackService.saveFeedback(request, lecturerId), HttpStatus.CREATED);
    }

    // TODO: SECURITY
    @GetMapping(params = {"studentId", "assessmentId"})
    public ResponseEntity<FeedbackResponse> getFeedbackByAssessmentIdAndStudentId(
            @RequestParam Long studentId, @RequestParam Long assessmentId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByAssessmentIdAndStudentId(studentId, assessmentId));
    }

    // TODO: SECURITY
    @GetMapping(params = {"studentId", "!assessmentId"})
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByStudentId(
            @RequestParam Long studentId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByStudentId(studentId));
    }

    // TODO: SECURITY
    @GetMapping(path = "/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getFeedbackById(id));
    }
}
