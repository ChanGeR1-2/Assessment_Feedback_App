package com.dissertation.backend.feedback_queries;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.feedback.Feedback;
import com.dissertation.backend.feedback.FeedbackRepository;
import com.dissertation.backend.feedback.dto.*;
import com.dissertation.backend.feedback.exceptions.FeedbackNotFoundException;
import com.dissertation.backend.feedback_queries.dto.CreateFeedbackQueryAnswerRequest;
import com.dissertation.backend.feedback_queries.dto.CreateFeedbackQueryRequest;
import com.dissertation.backend.feedback_queries.dto.FeedbackQueryAnswerResponse;
import com.dissertation.backend.feedback_queries.dto.FeedbackQueryResponse;
import com.dissertation.backend.feedback_queries.exceptions.FeedbackQueryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackQueryService {
    private final FeedbackQueryRepository feedbackQueryRepository;
    private final FeedbackQueryAnswerRepository feedbackQueryAnswerRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    public FeedbackQueryService(FeedbackQueryRepository feedbackQueryRepository, FeedbackQueryAnswerRepository feedbackQueryAnswerRepository, FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackQueryRepository = feedbackQueryRepository;
        this.feedbackQueryAnswerRepository = feedbackQueryAnswerRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<FeedbackQueryResponse> getFeedbackQueryByFeedbackId(Long feedbackId) {
        return feedbackQueryRepository.findByFeedbackId(feedbackId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PendingQueryResponse> getUnansweredFeedbackByLecturerId(Long lecturerId) {
        return feedbackQueryRepository.findUnansweredByLecturerId(lecturerId)
                .stream()
                .map(this::toPendingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackQueryResponse> getFeedbackQueriesByStudentId(Long studentId, AppUserDetails principal) {
        if (principal.getRole() == UserRole.STUDENT && !principal.getId().equals(studentId)) {
            throw new ForbiddenException("You are not authorised to view this list of feedback queries.");
        }
        return feedbackQueryRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FeedbackQueryResponse createFeedbackQuery(Long feedbackId, CreateFeedbackQueryRequest request, Long studentId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));
        FeedbackQuery query = new FeedbackQuery(
                request.query(),
                feedback,
                student
        );
        return toResponse(feedbackQueryRepository.save(query));
    }

    public FeedbackQueryResponse answerFeedbackQuery(CreateFeedbackQueryAnswerRequest request, Long feedbackQueryId, Long lecturerId) {
        FeedbackQuery query = feedbackQueryRepository.findById(feedbackQueryId)
                .orElseThrow(() -> new FeedbackQueryNotFoundException(feedbackQueryId));
        AppUser lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new UserNotFoundException(lecturerId));
        FeedbackQueryAnswer answer = new FeedbackQueryAnswer(
                request.answer(),
                query,
                lecturer
        );

        feedbackQueryAnswerRepository.save(answer);
        return toResponse(query);
    }

    private FeedbackQueryResponse toResponse(FeedbackQuery feedbackQuery) {
        FeedbackQueryAnswer answer = feedbackQueryAnswerRepository.findByFeedbackQueryId(feedbackQuery.getId()).orElse(null);
        return new FeedbackQueryResponse(feedbackQuery.getId(),
                feedbackQuery.getFeedback().getId(),
                feedbackQuery.getQuery(),
                feedbackQuery.getStudent().getFullName(),
                feedbackQuery.getCreatedAt(),
                answer != null ? toAnswerResponse(answer)
                        : null
                );
    }

    private PendingQueryResponse toPendingResponse(FeedbackQuery feedbackQuery) {
        return new PendingQueryResponse(
                feedbackQuery.getId(),
                feedbackQuery.getFeedback().getId(),
                feedbackQuery.getQuery(),
                feedbackQuery.getStudent().getFullName(),
                feedbackQuery.getFeedback().getAssessment().getTitle(),
                feedbackQuery.getCreatedAt()
        );
    }

    private FeedbackQueryAnswerResponse toAnswerResponse(FeedbackQueryAnswer feedbackQueryAnswer) {
        return new FeedbackQueryAnswerResponse(
                feedbackQueryAnswer.getId(),
                feedbackQueryAnswer.getAnswer(),
                feedbackQueryAnswer.getLecturer().getFullName(),
                feedbackQueryAnswer.getCreatedAt()
        );
    }
}
