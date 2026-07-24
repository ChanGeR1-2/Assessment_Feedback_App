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
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.common.exceptions.InvalidRoleException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.dto.CreateFeedbackItemRequest;
import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackItemResponse;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import com.dissertation.backend.feedback.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    public List<FeedbackResponse> getFeedbackByStudentId(Long studentId, AppUserDetails userDetails) {
        if (userDetails.getRole() == UserRole.STUDENT
                && !Objects.equals(userDetails.getId(), studentId)) {
            throw new ForbiddenException("You are not authorised to view this feedback.");
        }
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
    public FeedbackResponse getFeedbackById(Long feedbackId, AppUserDetails userDetails) {
        Feedback feedback = feedbackRepository.findByIdWithDetails(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));
        switch (userDetails.getRole()) {
            case LECTURER:
                if (!Objects.equals(userDetails.getId(), feedback.getLecturer().getId())) {
                    throw new ForbiddenException("You are not authorised to view this feedback.");
                }
                break;
            case STUDENT:
                if (!Objects.equals(userDetails.getId(), feedback.getStudent().getId())) {
                    throw new ForbiddenException("You are not authorised to view this feedback.");
                }
                break;
            default:
                throw new ForbiddenException("You are not authorised to view this feedback.");
        }

        return toResponse(feedback);
    }

    @Transactional(readOnly = true)
    public Optional<FeedbackResponse> getFeedbackByAssessmentIdAndStudentId(Long studentId, Long assessmentId, AppUserDetails userDetails) {
        Optional<Feedback> feedback = feedbackRepository.findByAssessmentIdAndStudentId(assessmentId, studentId);
        feedback.ifPresent((f -> {
            if (!Objects.equals(userDetails.getId(), f.getLecturer().getId())) {
                throw new ForbiddenException("You are not authorised to view this feedback.");
            }
        }));
        return feedback.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByAssessmentId(Long assessmentId, AppUserDetails userDetails) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));
        if (assessment.getModule().getLecturer() == null ||
        !Objects.equals(userDetails.getId(), assessment.getModule().getLecturer().getId())) {
            throw new ForbiddenException("You are not authorised to view this feedback.");
        }
        return feedbackRepository.findByAssessmentId(assessmentId).stream()
                .map(this::toResponse)
                .toList();
    }

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

        if (module.getLecturer() == null
                || !Objects.equals(module.getLecturer().getId(), lecturer.getId())) {
            throw new ForbiddenException("You are not authorised to submit feedback for this module.");
        }

        if (feedbackRepository.existsByAssessmentIdAndStudentId(assessment.getId(), student.getId())) {
            throw new FeedbackExistsException(assessment.getId(), student.getId());
        }

        if (!enrolmentRepository.existsByStudentIdAndModuleId(student.getId(), module.getId())) {
            throw new StudentNotEnrolledException(student.getId(), module.getId());
        }

        Map<Long, MarkingItem> markingItems = markingItemRepository
                .findByAssessmentIdOrderByPosition(assessment.getId()).stream()
                .collect(Collectors.toMap(MarkingItem::getId, mi -> mi));

        List<CreateFeedbackItemRequest> items = createFeedbackRequest.items();

        Set<Long> submittedIds = items.stream()
                .map(CreateFeedbackItemRequest::markingItemId)
                .collect(Collectors.toSet());

        if (submittedIds.size() != items.size()) {
            throw new DuplicateMarkingItemException("Feedback contains duplicate marking items.");
        }

        if (!submittedIds.equals(markingItems.keySet())) {
            throw new IncompleteFeedbackException(
                    "Submitted feedback does not cover all assessment items.");
        }

        short totalAwarded = (short) items.stream()
                .mapToInt(CreateFeedbackItemRequest::awardedMark)
                .sum();

        Feedback feedback = new Feedback(student, lecturer, assessment, totalAwarded,
                createFeedbackRequest.summary());

        for (CreateFeedbackItemRequest item : items) {
            MarkingItem markingItem = markingItems.get(item.markingItemId());
            if (item.awardedMark() > markingItem.getMaxMark()) {
                throw new InvalidMarkException(
                        item.awardedMark(), markingItem.getMaxMark(), item.markingItemId());
            }
            feedback.addItem(new FeedbackItem(feedback, markingItem, item.awardedMark(), item.comment()));
        }

        return toResponse(feedbackRepository.save(feedback));
    }
}
