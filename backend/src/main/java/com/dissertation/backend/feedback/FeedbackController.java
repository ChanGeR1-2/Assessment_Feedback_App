package com.dissertation.backend.feedback;

import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import com.dissertation.backend.feedback_audio.AudioStorageService;
import com.dissertation.backend.feedback_audio.FeedbackAudio;
import com.dissertation.backend.feedback_audio.FeedbackAudioService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class FeedbackController {
    private final FeedbackService feedbackService;
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/api/feedback")
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request,
                                                           @RequestParam(defaultValue = "false") boolean publish,
                                                           @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(feedbackService.saveFeedback(request, principal.getId(), publish), HttpStatus.CREATED);
    }

    @PatchMapping("/api/feedback/{feedbackId}/publish")
    public ResponseEntity<Void> publishFeedback(@PathVariable Long feedbackId, @AuthenticationPrincipal AppUserDetails principal) {
        feedbackService.publishFeedback(feedbackId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/feedback/{feedbackId}")
    public ResponseEntity<FeedbackResponse> editFeedback(@PathVariable Long feedbackId,
                                                         @Valid @RequestBody CreateFeedbackRequest request,
                                                         @RequestParam(defaultValue = "false") boolean publish,
                                                         @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(feedbackService.updateFeedback(feedbackId, request, principal.getId(), publish), HttpStatus.OK);
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
