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

/**
 * Manages the recording, retrieval and publication of assessment feedback.
 *
 * <p>Authorisation is enforced here rather than in the controller, since access
 * depends on the data: a lecturer may only see feedback they authored, and a
 * student only their own. Draft feedback is invisible to students — reads by a
 * student return {@link FeedbackNotFoundException} rather than a forbidden
 * response, so that the existence of unpublished feedback is not disclosed.
 *
 * <p>Feedback must cover an assessment's marking scheme exactly: every marking
 * item, once each. The overall mark is derived from the awarded item marks
 * rather than supplied by the caller, so the total can never disagree with the
 * breakdown. Feedback is editable only while in {@link FeedbackStatus#DRAFT};
 * publishing is one-way.
 */
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

    /**
     * Converts a feedback to a response object.
     * @param feedback the feedback to convert
     * @return the feedback response.
     */
    private FeedbackResponse toResponse(Feedback feedback) {
        AppUser lecturer = feedback.getLecturer();
        Assessment assessment = feedback.getAssessment();

        List<FeedbackItemResponse> itemResponses = feedback.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getMarkingItem().getPosition())) // sort by position for frontend
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

    /**
     * Gets all feedback for a student.
     * @param studentId the student id
     * @param userDetails the user details - taken from the security context.
     * @return a list of feedback responses.
     * @throws ForbiddenException if the user is not authorised to view the feedback.
     * @throws UserNotFoundException if the student does not exist.
     * @throws InvalidRoleException if the user is not a student.
     */
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

        // Only finds published feedbacks as students shouldn't be able to see drafts.
        List<Feedback> feedbacks = feedbackRepository.findByStudentId(studentId, FeedbackStatus.PUBLISHED);
        return feedbacks.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Gets feedback by id
     * @param feedbackId the feedback id
     * @param userDetails the user details - taken from the security context.
     * @return the feedback response.
     * @throws ForbiddenException if the user is not authorised to view the feedback.
     * A lecturer must be the author of the feedback, and a student must be the recipient of the feedback.
     * @throws FeedbackNotFoundException if the feedback does not exist or if the user is a student and the feedback is not published.
     */
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

    /**
     * Gets feedback by assessment id and student id.
     * @param studentId the student id
     * @param assessmentId the assessment id
     * @param userDetails the user details - taken from the security context.
     * @return the feedback response - optional so the controller can respond with null instead of a 404.
     * @throws ForbiddenException if the user is not authorised to view the feedback - they must be the lecturer of the assessment.
     */
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

    /**
     * Gets feedback by assessment id
     * @param assessmentId the assessment id
     * @param userDetails the user details - taken from the security context.
     * @return a list of feedback responses.
     * @throws ForbiddenException if the user is not authorised to view the feedback - they must be the lecturer of the assessment.
     * @throws AssessmentNotFoundException if the assessment does not exist.
     */
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

    /**
     * Publish feedback without editing it.
     * @param feedbackId the feedback id
     * @param lecturerId the lecturer id
     * @return the feedback response.
     * @throws ForbiddenException if the user is not authorised to publish the feedback.
     * @throws FeedbackNotFoundException if the feedback does not exist.
     */
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

    /**
     * Save feedback.
     * @param createFeedbackRequest the feedback request
     * @param lecturerId the lecturer id
     * @param publish whether to publish the feedback immediately
     * @return the feedback response.
     * @throws ForbiddenException if the user is not authorised to submit feedback for the module.
     * @throws UserNotFoundException if the student or lecturer does not exist.
     * @throws InvalidRoleException if the user is not a lecturer.
     * @throws AssessmentNotFoundException if the assessment does not exist.
     * @throws StudentNotEnrolledException if the student is not enrolled in the module.
     * @throws FeedbackExistsException if the student has already submitted feedback for the assessment.
     * @throws DuplicateTagException if the feedback contains duplicate tags.
     * @throws TagNotFoundException if one or more tags do not exist. i.e. Not in the database - only existing tags can be used as feedback tags.
     * @throws DuplicateMarkingItemException if the feedback contains duplicate marking items.
     * @throws IncompleteFeedbackException if the feedback does not cover all assessment items.
     * @throws InvalidMarkException if the mark is greater than the maximum mark for the marking item.
     */
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

        Feedback feedback = new Feedback(student, lecturer, assessment, (short) 0, // initial mark is 0
                createFeedbackRequest.summary());

        feedback.setMark(validateAndBuildItems(feedback, assessment, createFeedbackRequest.items()));
        validateAndBuildTags(feedback, createFeedbackRequest.tags() == null ? List.of() : createFeedbackRequest.tags());
        if (publish) {
            feedback.publish();
        }
        return toResponse(feedbackRepository.save(feedback));
    }

    /**
     * Update feedback.
     * @param feedbackId the feedback id
     * @param request the feedback request
     * @param lecturerId the lecturer id
     * @param publish whether to publish the feedback immediately
     * @return the feedback response.
     * @throws ForbiddenException if the user is not authorised to edit the feedback.
     * @throws FeedbackNotFoundException if the feedback does not exist.
     * @throws FeedbackNotEditableException if the feedback is not in draft status.
     */
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

    /**
     * Takes feedback and the tags request, validates the tags and attaches them to the feedback.
     * @param feedback the feedback to attach the tags to
     * @param tags the tags request
     * @throws DuplicateTagException if the tags contain duplicate tags.
     * @throws TagNotFoundException if one or more tags do not exist. i.e. Not in the database - only existing tags can be used as feedback tags.
     */
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

        feedback.getTags().clear(); // clear any existing tags and repopulate with new ones
        feedbackRepository.flush(); // prevent transient unique-constraint violation
        for (CreateFeedbackTagRequest tag : tags) {
            feedback.addTag(new FeedbackTag(tagsById.get(tag.tagId()), feedback, tag.tagType()));
        }
    }

    /**
     * Validates the feedback items and attaches them to the feedback.
     * @param feedback the feedback to attach the items to
     * @param assessment the assessment the feedback is for
     * @param items the feedback items request
     * @return the total mark of the feedback
     * @throws DuplicateMarkingItemException if the feedback contains duplicate marking items.
     * @throws IncompleteFeedbackException if the feedback does not cover all assessment items.
     * @throws InvalidMarkException if the mark is greater than the maximum mark for the marking item.
     */
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

        feedback.getItems().clear(); // clear any existing items and repopulate with new ones
        feedbackRepository.flush(); // prevent transient unique-constraint violation
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
