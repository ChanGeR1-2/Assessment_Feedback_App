package com.dissertation.backend.feedback;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.assessments.Assessment;
import com.dissertation.backend.assessments.AssessmentRepository;
import com.dissertation.backend.assessments.MarkingItem;
import com.dissertation.backend.assessments.MarkingItemRepository;
import com.dissertation.backend.assessments.exceptions.AssessmentNotFoundException;
import com.dissertation.backend.assessments.exceptions.MarkingItemNotFoundException;
import com.dissertation.backend.common.exceptions.InvalidRoleException;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.dto.CreateFeedbackItemRequest;
import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackItemResponse;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import com.dissertation.backend.feedback.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final MarkingItemRepository markingItemRepository;
    public FeedbackService(FeedbackRepository feedbackRepository, AssessmentRepository assessmentRepository, UserRepository userRepository, EnrolmentRepository enrolmentRepository, MarkingItemRepository markingItemRepository) {
        this.feedbackRepository = feedbackRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.markingItemRepository = markingItemRepository;
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        AppUser lecturer = feedback.getLecturer();
        Assessment assessment = feedback.getAssessment();

        List<FeedbackItemResponse> itemResponses = feedback.getItems().stream()
                .map(item -> new FeedbackItemResponse(
                        item.getId(),
                        feedback.getId(),
                        item.getMarkingItem().getId(),
                        item.getMarkingItem().getName(),
                        item.getAwardedMark(),
                        item.getMarkingItem().getMaxMark(),
                        item.getComment()))
                .toList();

        Integer totalMark = feedback.getItems().stream()
                .mapToInt(i -> i.getMarkingItem().getMaxMark())
                .sum();

        return new FeedbackResponse(
                feedback.getId(),
                assessment.getTitle(),
                assessment.getId(),
                feedback.getStudent().getFullName(),
                feedback.getStudent().getId(),
                lecturer.getFullName(),
                lecturer.getId(),
                feedback.getMark(),
                totalMark,
                feedback.getSummary(),
                feedback.getCreatedAt(),
                itemResponses);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByStudentId(Long studentId) {
        AppUser student = userRepository.findAppUserById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));

        if (student.getRole() != UserRole.STUDENT) {
            throw new InvalidRoleException(student.getId(), UserRole.STUDENT);
        }

        List<Feedback> feedbacks = feedbackRepository.findByStudentId(studentId);
        return feedbacks.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(Long feedbackId) {
        return feedbackRepository.findByIdWithDetails(feedbackId)
                .map(this::toResponse)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackByAssessmentIdAndStudentId(Long studentId, Long assessmentId) {
        return feedbackRepository.findByAssessmentIdAndStudentId(assessmentId, studentId)
                .map(this::toResponse)
                .orElseThrow(() -> new FeedbackNotFoundException(assessmentId, studentId));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByAssessmentId(Long assessmentId) {
        return feedbackRepository.findByAssessmentId(assessmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    // TODO: SECURITY
    // TODO: N + 1 optimisation
    @Transactional
    public FeedbackResponse saveFeedback(CreateFeedbackRequest createFeedbackRequest, Long lecturerId) {
        AppUser student = userRepository.findAppUserById(createFeedbackRequest.studentId())
                .orElseThrow(() -> new UserNotFoundException(createFeedbackRequest.studentId()));

        if (student.getRole() != UserRole.STUDENT) {
            throw new InvalidRoleException(student.getId(), UserRole.STUDENT);
        }

        Assessment assessment = assessmentRepository.findById(createFeedbackRequest.assessmentId())
                .orElseThrow(() -> new AssessmentNotFoundException(createFeedbackRequest.assessmentId()));

        CourseModule module = assessment.getModule();

        AppUser lecturer = userRepository.findAppUserById(lecturerId)
                .orElseThrow(() -> new UserNotFoundException(lecturerId));

        if (lecturer.getRole() != UserRole.LECTURER) {
            throw new InvalidRoleException(lecturer.getId(), UserRole.LECTURER);
        }

        if (!Objects.equals(module.getLecturer().getId(), lecturer.getId())) {
            throw new UnauthorisedLecturerException(lecturerId, module.getId());
        }

        if (feedbackRepository.existsByAssessmentIdAndStudentId(assessment.getId(), student.getId())) {
            throw new FeedbackExistsException(assessment.getId(), student.getId());
        }

        if (!enrolmentRepository.existsByStudentIdAndModuleId(student.getId(), module.getId())) {
            throw new StudentNotEnrolledException(student.getId(), module.getId());
        }

        short totalAwarded = (short) createFeedbackRequest.items().stream()
                .mapToInt(CreateFeedbackItemRequest::awardedMark)
                .sum();

        Feedback feedback = new Feedback(student, lecturer, assessment, totalAwarded, createFeedbackRequest.summary());

        Set<Long> seen = new HashSet<>();

        createFeedbackRequest.items()
                .forEach((item) -> {
                    if (!seen.add(item.markingItemId())) {
                        throw new DuplicateMarkingItemException(item.markingItemId());
                    }
                    MarkingItem markingItem = markingItemRepository.findById(item.markingItemId())
                            .orElseThrow(() -> new MarkingItemNotFoundException(item.markingItemId()));
                    if (!markingItem.getAssessment().getId().equals(assessment.getId())) {
                        throw new MarkingItemNotForAssessmentException(item.markingItemId(), assessment.getId());
                    }
                    if (item.awardedMark() > markingItem.getMaxMark()) {
                        throw new InvalidMarkException(item.awardedMark(), markingItem.getMaxMark(), item.markingItemId());
                    }
                    feedback.addItem(new FeedbackItem(feedback, markingItem, item.awardedMark(), item.comment()));
                });

        Set<Long> assessmentItemIds = markingItemRepository.findByAssessmentIdOrderByPosition(assessment.getId()).stream()
                .map(MarkingItem::getId).collect(Collectors.toSet());
        Set<Long> submittedIds = createFeedbackRequest.items().stream()
                .map(CreateFeedbackItemRequest::markingItemId).collect(Collectors.toSet());

        if (!submittedIds.equals(assessmentItemIds)) {
            throw new IncompleteFeedbackException("Submitted feedback does not cover all assessment items.");
        }

        Feedback savedFeedback = feedbackRepository.save(feedback);

        return toResponse(savedFeedback);
    }
}
