package com.dissertation.backend.feedback_queries;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.feedback.Feedback;
import com.dissertation.backend.feedback.FeedbackRepository;
import com.dissertation.backend.feedback.exceptions.FeedbackNotFoundException;
import com.dissertation.backend.feedback_queries.dto.*;
import com.dissertation.backend.feedback_queries.exceptions.FeedbackQueryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages the retrieval and creation of feedback queries and answers.
 *
 * <p> Authorisation is enforced here rather than in the controller, since access
 * depends on the data: a lecturer may only view and answer queries attached to the feedback they authored, and
 * a student may only make queries and see query answers on their own feedback.
 *
 * <p> Feedback queries are immutable once they have been answered, so that
 * answers cannot be changed by a later change to the feedback.
 * 1. Feedback queries are created by students, and are attached to a feedback.
 * 2. Lecturers can view and answer feedback queries attached to their feedback.
 * 3. Students can view the answers to their own feedback queries.
 */
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

    /**
     * Get feedback query by feedback id.
     * Returns an optional so the controller can respond with null instead of a 404.
     * @param feedbackId the feedback id
     * @return the feedback query response.
     */
    @Transactional(readOnly = true)
    public Optional<FeedbackQueryResponse> getFeedbackQueryByFeedbackId(Long feedbackId) {
        return feedbackQueryRepository.findByFeedbackId(feedbackId).map(this::toResponse);
    }

    /**
     * Get all feedback queries that have not been answered by the lecturer.
     * Responds with a separate DTO as unanswered queries naturally do not have answers yet and the assessment title is needed.
     * @param lecturerId the lecturer id
     * @return the list of feedback queries.
     */
    @Transactional(readOnly = true)
    public List<PendingQueryResponse> getUnansweredFeedbackByLecturerId(Long lecturerId) {
        return feedbackQueryRepository.findUnansweredByLecturerId(lecturerId)
                .stream()
                .map(this::toPendingResponse)
                .toList();
    }

    /**
     * Get all feedback queries for a student.
     * @param studentId the student id
     * @param principal the authenticated user - taken from the security context.
     * @return the list of feedback queries.
     * @throws ForbiddenException if the user is not authorised to view the feedback queries.
     */
    @Transactional(readOnly = true)
    public List<FeedbackQueryResponse> getFeedbackQueriesByStudentId(Long studentId, AppUserDetails principal) {
        if (principal.getRole() == UserRole.STUDENT && !principal.getId().equals(studentId)) {
            throw new ForbiddenException("You are not authorised to view this list of feedback queries.");
        }
        return feedbackQueryRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Create a feedback query.
     * @param feedbackId the feedback id
     * @param request the feedback query request
     * @param studentId the student id
     * @return the feedback query response.
     * @throws FeedbackNotFoundException if the feedback does not exist.
     * @throws UserNotFoundException if the student does not exist.
     * @throws ForbiddenException if the user is not authorised to create a feedback query for the feedback.
     */
    @Transactional
    public FeedbackQueryResponse createFeedbackQuery(Long feedbackId, CreateFeedbackQueryRequest request, Long studentId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));
        if (!Objects.equals(feedback.getStudent().getId(), studentId)) {
            throw new ForbiddenException("You are not authorised to create a feedback query for this feedback.");
        }
        FeedbackQuery query = new FeedbackQuery(
                request.query(),
                feedback,
                student
        );
        return toResponse(feedbackQueryRepository.save(query));
    }

    /**
     * Answer a feedback query.
     * @param request the feedback query answer request
     * @param feedbackQueryId the feedback query id
     * @param lecturerId the lecturer id
     * @return the feedback query response.
     * @throws FeedbackQueryNotFoundException if the feedback query does not exist.
     * @throws UserNotFoundException if the lecturer does not exist.
     * @throws ForbiddenException if the user is not authorised to answer the feedback query.
     */
    public FeedbackQueryResponse answerFeedbackQuery(CreateFeedbackQueryAnswerRequest request, Long feedbackQueryId, Long lecturerId) {
        FeedbackQuery query = feedbackQueryRepository.findById(feedbackQueryId)
                .orElseThrow(() -> new FeedbackQueryNotFoundException(feedbackQueryId));
        AppUser lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new UserNotFoundException(lecturerId));
        if (!Objects.equals(query.getFeedback().getLecturer().getId(), lecturerId)) {
            throw new ForbiddenException("You are not authorised to answer this feedback query.");
        }
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
