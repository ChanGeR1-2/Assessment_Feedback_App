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
import com.dissertation.backend.tags.FeedbackTag;
import com.dissertation.backend.tags.Tag;
import com.dissertation.backend.tags.TagRepository;
import com.dissertation.backend.tags.dto.CreateFeedbackTagRequest;
import com.dissertation.backend.tags.dto.FeedbackTagResponse;
import com.dissertation.backend.tags.exceptions.DuplicateTagException;
import com.dissertation.backend.tags.exceptions.TagNotFoundException;
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
    private final TagRepository tagRepository;
    public FeedbackService(FeedbackRepository feedbackRepository, AssessmentRepository assessmentRepository, UserRepository userRepository, EnrolmentRepository enrolmentRepository, MarkingItemRepository markingItemRepository, TagRepository tagRepository) {
        this.feedbackRepository = feedbackRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.markingItemRepository = markingItemRepository;
        this.tagRepository = tagRepository;
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        AppUser lecturer = feedback.getLecturer();
        Assessment assessment = feedback.getAssessment();

        List<FeedbackItemResponse> itemResponses = feedback.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getMarkingItem().getPosition()))
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

        List<FeedbackTagResponse> tagResponses = feedback.getTags().stream()
                .map(ft -> new FeedbackTagResponse(
                        ft.getId(),
                        ft.getTag().getId(),
                        ft.getTag().getName(),
                        ft.getTagType()))
                .toList();

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
                itemResponses,
                feedback.getAssessment().getModule().getId(),
                feedback.getAssessment().getModule().getTitle(),
                feedback.getAssessment().getModule().getAcademicYear(),
                feedback.getStatus(),
                tagResponses
        );
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

        List<Feedback> feedbacks = feedbackRepository.findByStudentId(studentId, FeedbackStatus.PUBLISHED);
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
                if (feedback.getStatus() != FeedbackStatus.PUBLISHED) {
                    throw new FeedbackNotFoundException(feedbackId);
                }
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
    public FeedbackResponse publishFeedback(Long feedbackId, Long lecturerId) {
        Feedback feedback = feedbackRepository.findByIdWithDetails(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        if (!Objects.equals(feedback.getLecturer().getId(), lecturerId)) {
            throw new ForbiddenException("You are not authorised to publish this feedback.");
        }

        feedback.publish();
        return toResponse(feedback);
    }

    @Transactional
    public FeedbackResponse saveFeedback(CreateFeedbackRequest createFeedbackRequest, Long lecturerId, boolean publish) {
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

        Feedback feedback = new Feedback(student, lecturer, assessment, (short) 0,
                createFeedbackRequest.summary());

        feedback.setMark(validateAndBuildItems(feedback, assessment, createFeedbackRequest.items()));
        validateAndBuildTags(feedback, createFeedbackRequest.tags() == null ? List.of() : createFeedbackRequest.tags());
        if (publish) {
            feedback.publish();
        }
        return toResponse(feedbackRepository.save(feedback));
    }

    @Transactional
    public FeedbackResponse updateFeedback(Long feedbackId, CreateFeedbackRequest request, Long lecturerId, boolean publish) {
        Feedback feedback = feedbackRepository.findByIdWithDetails(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        if (!Objects.equals(feedback.getLecturer().getId(), lecturerId)) {
            throw new ForbiddenException("You are not authorised to edit this feedback.");
        }

        if (feedback.getStatus() != FeedbackStatus.DRAFT) {
            throw new FeedbackNotEditableException(feedbackId);
        }

        feedback.setMark(validateAndBuildItems(feedback, feedback.getAssessment(), request.items()));
        validateAndBuildTags(feedback, request.tags() == null ? List.of() : request.tags());
        feedback.setSummary(request.summary());
        if (publish) {
            feedback.publish();
        }

        return toResponse(feedback);
    }

    private void validateAndBuildTags(Feedback feedback, List<CreateFeedbackTagRequest> tags) {
        Set<Long> tagIds = tags.stream()
                .map(CreateFeedbackTagRequest::tagId)
                .collect(Collectors.toSet());

        if (tagIds.size() != tags.size()) {
            throw new DuplicateTagException("Feedback contains duplicate tags.");
        }

        Map<Long, Tag> tagsById = tagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        if (tagsById.size() != tagIds.size()) {
            throw new TagNotFoundException("One or more tags do not exist.");
        }

        feedback.getTags().clear();
        feedbackRepository.flush();
        for (CreateFeedbackTagRequest tag : tags) {
            feedback.addTag(new FeedbackTag(tagsById.get(tag.tagId()), feedback, tag.tagType()));
        }
    }

    private short validateAndBuildItems(Feedback feedback, Assessment assessment,
                                        List<CreateFeedbackItemRequest> items) {
        Map<Long, MarkingItem> markingItems = markingItemRepository
                .findByAssessmentIdOrderByPosition(assessment.getId()).stream()
                .collect(Collectors.toMap(MarkingItem::getId, mi -> mi));

        Set<Long> submittedIds = items.stream()
                .map(CreateFeedbackItemRequest::markingItemId).collect(Collectors.toSet());

        if (submittedIds.size() != items.size()) {
            throw new DuplicateMarkingItemException("Feedback contains duplicate marking items.");
        }
        if (!submittedIds.equals(markingItems.keySet())) {
            throw new IncompleteFeedbackException("Submitted feedback does not cover all assessment items.");
        }

        feedback.getItems().clear();
        feedbackRepository.flush();
        short total = 0;
        for (CreateFeedbackItemRequest item : items) {
            MarkingItem markingItem = markingItems.get(item.markingItemId());
            if (item.awardedMark() > markingItem.getMaxMark()) {
                throw new InvalidMarkException(
                        item.awardedMark(), markingItem.getMaxMark(), item.markingItemId());
            }
            feedback.addItem(new FeedbackItem(feedback, markingItem, item.awardedMark(), item.comment()));
            total += item.awardedMark();
        }
        return total;
    }
}
