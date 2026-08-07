package com.dissertation.backend.feedback_queries;

import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.feedback.dto.*;
import com.dissertation.backend.feedback_queries.dto.CreateFeedbackQueryAnswerRequest;
import com.dissertation.backend.feedback_queries.dto.CreateFeedbackQueryRequest;
import com.dissertation.backend.feedback_queries.dto.FeedbackQueryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FeedbackQueryController {
    private final FeedbackQueryService feedbackQueryService;
    public FeedbackQueryController(FeedbackQueryService feedbackQueryService) {
        this.feedbackQueryService = feedbackQueryService;
    }

    @GetMapping("/feedback/{feedbackId}/feedback-queries")
    public ResponseEntity<FeedbackQueryResponse> getFeedbackQueryByFeedbackId(@PathVariable Long feedbackId) {
        return feedbackQueryService.getFeedbackQueryByFeedbackId(feedbackId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/students/{studentId}/feedback-queries")
    public ResponseEntity<List<FeedbackQueryResponse>> getFeedbackQueryByStudentId(@PathVariable Long studentId, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(feedbackQueryService.getFeedbackQueriesByStudentId(studentId, principal));
    }

    @GetMapping("/lecturer/feedback-queries")
    public ResponseEntity<List<PendingQueryResponse>> getUnansweredFeedbackByLecturerId(@AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(feedbackQueryService.getUnansweredFeedbackByLecturerId(principal.getId()));
    }

    @PostMapping("/feedback/{feedbackId}/feedback-queries")
    public ResponseEntity<FeedbackQueryResponse> createFeedbackQuery(@PathVariable Long feedbackId,
                                                                    @Valid @RequestBody CreateFeedbackQueryRequest request,
                                                                     @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(feedbackQueryService.createFeedbackQuery(feedbackId, request, principal.getId()), HttpStatus.CREATED);
    }

    @PostMapping("/feedback-queries/{feedbackQueryId}/answer")
    public ResponseEntity<FeedbackQueryResponse> answerFeedbackQuery(@PathVariable Long feedbackQueryId,
                                                                           @Valid @RequestBody CreateFeedbackQueryAnswerRequest request,
                                                                           @AuthenticationPrincipal AppUserDetails principal
    ) {
        return new ResponseEntity<>(feedbackQueryService.answerFeedbackQuery(request, feedbackQueryId, principal.getId()), HttpStatus.CREATED);
    }

}
