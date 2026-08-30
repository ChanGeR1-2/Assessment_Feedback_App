package com.dissertation.backend.assessments;

import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.assessments.dto.*;
import com.dissertation.backend.assessments.exceptions.*;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.FeedbackRepository;
import com.dissertation.backend.feedback.FeedbackStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Manages assessments and their marking schemes.
 *
 * <p>Authorisation is enforced here rather than in the controller, since access
 * depends on the data: admins see everything, lecturers only assessments on
 * modules they own, and students only assessments on modules they are enrolled on.
 *
 * <p>A marking scheme becomes immutable once any feedback exists for its
 * assessment ({@link #isRubricLocked}), so that recorded feedback cannot be
 * invalidated by a later change to the criteria it was marked against.
 * Reordering is exempt, as position is presentational only.
 */
@Service
public class AssessmentService {
    private final AssessmentRepository assessmentRepository;
    private final ModuleRepository moduleRepository;
    private final MarkingItemRepository markingItemRepository;
    private final FeedbackRepository feedbackRepository;
    private final EnrolmentRepository enrolmentRepository;
    public AssessmentService(AssessmentRepository assessmentRepository, ModuleRepository moduleRepository, MarkingItemRepository markingItemRepository, FeedbackRepository feedbackRepository, EnrolmentRepository enrolmentRepository) {
        this.assessmentRepository = assessmentRepository;
        this.moduleRepository = moduleRepository;
        this.markingItemRepository = markingItemRepository;
        this.feedbackRepository = feedbackRepository;
        this.enrolmentRepository = enrolmentRepository;
    }

    /**
     * Returns all assessments for the given principal.
     * @param principal the authenticated user - taken from the security context.
     * @return List of assessments.
     */
    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAllAssessments(AppUserDetails principal) {
        return switch (principal.getRole()) {
            case ADMIN -> assessmentRepository.findAll().stream()
                    .map((a) -> assessmentToResponse(a, a.getModule().getAcademicYear()))
                    .toList();
            case LECTURER -> assessmentRepository.findByLecturerId(principal.getId()).stream()
                    .map((a) -> assessmentToResponse(a, a.getModule().getAcademicYear()))
                    .toList();
            case STUDENT -> assessmentRepository.findByStudentId(principal.getId()).stream()
                    .map((a) -> assessmentToResponse(a, a.getModule().getAcademicYear()))
                    .toList();
        };
    }

    /**
     * Returns all assessments for the given module.
     * @param moduleId the module id
     * @param principal the authenticated user - taken from the security context.
     * @return List of assessments.
     * @throws ForbiddenException if the user is not authorised to view the module.
     */
    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAssessmentsByModuleId(Long moduleId, AppUserDetails principal) {
        return switch (principal.getRole()) {
            case ADMIN -> assessmentRepository.findByModuleId(moduleId).stream()
                    .map((a) -> assessmentToResponse(a, a.getModule().getAcademicYear()))
                    .toList();
            case LECTURER -> {
                moduleRepository.findById(moduleId).ifPresent((module) -> {
                    if (module.getLecturer() == null || !module.getLecturer().getId().equals(principal.getId())) {
                        throw new ForbiddenException("You are not authorised to view this module.");
                    }
                });
                yield assessmentRepository.findByModuleId(moduleId).stream()
                        .map((a) -> assessmentToResponse(a, a.getModule().getAcademicYear()))
                        .toList();

            }
            case STUDENT -> {
                if (!enrolmentRepository.existsByStudentIdAndModuleId(principal.getId(), moduleId)) {
                    throw new ForbiddenException("You are not authorised to view this module.");
                }
                yield assessmentRepository.findByModuleId(moduleId).stream()
                        .map((a) -> assessmentToResponse(a, a.getModule().getAcademicYear()))
                        .toList();
            }
        };
    }

    /**
     * Returns an assessment by id.
     * @param id the assessment id
     * @param principal the authenticated user - taken from the security context.
     * @return the assessment.
     * @throws AssessmentNotFoundException if the assessment does not exist.
     * @throws ForbiddenException if the user is not authorised to view the assessment.
     */
    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long id, AppUserDetails principal) {
        return switch (principal.getRole()) {
            case ADMIN -> assessmentRepository.findById(id)
                    .map((a) -> assessmentToResponse(a, a.getModule().getAcademicYear()))
                    .orElseThrow(() -> new AssessmentNotFoundException(id));
            case LECTURER -> {
                Assessment assessment = assessmentRepository.findById(id)
                        .orElseThrow(() -> new AssessmentNotFoundException(id));
                if (assessment.getModule().getLecturer() == null || !assessment.getModule().getLecturer().getId().equals(principal.getId())) {
                    throw new ForbiddenException("You are not authorised to view this assessment.");
                }
                yield assessmentToResponse(assessment, assessment.getModule().getAcademicYear());
            }
            case STUDENT -> {
                Assessment assessment = assessmentRepository.findById(id)
                        .orElseThrow(() -> new AssessmentNotFoundException(id));
                if (!enrolmentRepository.existsByStudentIdAndModuleId(principal.getId(), assessment.getModule().getId())) {
                    throw new ForbiddenException("You are not authorised to view this assessment.");
                }
                yield assessmentToResponse(assessment, assessment.getModule().getAcademicYear());
            }
        };
    }

    /**
     * Creates an assessment - currently API-only as the frontend does not support this since it is out of scope for the MVP.
     * @param request the assessment request
     * @param principal the authenticated user - taken from the security context.
     * @return the created assessment.
     * @throws InvalidModuleException if the module does not exist.
     * @throws ForbiddenException if the user is not authorised to create an assessment - only admins can create assessments.
     */
    @Transactional
    public AssessmentResponse createAssessment(CreateAssessmentRequest request, AppUserDetails principal) {
        if (principal.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can create assessments.");
        }
        CourseModule module = moduleRepository.findById(request.moduleId())
                .orElseThrow(() -> new InvalidModuleException(request.moduleId()));

        Assessment assessment = new Assessment(request.title(), request.dueDate(), module, request.weight(), request.feedbackDueDate());
        return assessmentToResponse(assessmentRepository.save(assessment), module.getAcademicYear());
    }

    /**
     * Creates a marking item for an assessment.
     * @param assessmentId the assessment id
     * @param request the marking item request
     * @param principal the authenticated user - taken from the security context.
     * @return the created marking item.
     * @throws AssessmentNotFoundException if the assessment does not exist.
     * @throws ForbiddenException if the user is not authorised to create a marking item for the given assessment - they must be the module lecturer.
     * @throws RubricLockedException if the assessment is locked.
     */
    @Transactional
    public MarkingItemResponse createMarkingItem(Long assessmentId, CreateMarkingItemRequest request, AppUserDetails principal) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));

        if (assessment.getModule().getLecturer() == null || !assessment.getModule().getLecturer().getId().equals(principal.getId())) {
            throw new ForbiddenException("You are not authorised to create marking items for this assessment.");
        }

        if (isRubricLocked(assessmentId)) {
            throw new RubricLockedException(assessmentId);
        }

        MarkingItem markingItem = new MarkingItem(assessment, request.name(), request.maxMark(), request.position());
        return markingItemToResponse(markingItemRepository.save(markingItem), assessmentId);
    }

    /**
     * Edits a marking item.
     * @param assessmentId the assessment id
     * @param markingItemId the marking item id
     * @param request the marking item request
     * @param principal the authenticated user - taken from the security context.
     * @return the edited marking item.
     * @throws MarkingItemNotFoundException if the marking item does not exist.
     * @throws MarkingItemNotForAssessmentException if the marking item is not for the given assessment.
     * @throws ForbiddenException if the user is not authorised to edit the marking item - they must be the module lecturer.
     * @throws RubricLockedException if the assessment is locked.
     */
    @Transactional
    public MarkingItemResponse editMarkingItem(Long assessmentId, Long markingItemId, EditMarkingItemRequest request, AppUserDetails principal) {
        MarkingItem item = markingItemRepository.findById(markingItemId)
                .orElseThrow(() -> new MarkingItemNotFoundException(markingItemId));

        if (item.getAssessment().getModule().getLecturer() == null || !item.getAssessment().getModule().getLecturer().getId().equals(principal.getId())) {
            throw new ForbiddenException("You are not authorised to edit this marking item.");
        }

        if (!item.getAssessment().getId().equals(assessmentId)) {
            throw new MarkingItemNotForAssessmentException(markingItemId, assessmentId);
        }

        if (isRubricLocked(assessmentId)) {
            throw new RubricLockedException(assessmentId);
        }

        item.setName(request.name());
        item.setMaxMark(request.maxMark());
        return markingItemToResponse(markingItemRepository.save(item), assessmentId);
    }

    /**
     * Deletes a marking item.
     * @param assessmentId the assessment id
     * @param markingItemId the marking item id
     * @param principal the authenticated user - taken from the security context.
     * @throws MarkingItemNotFoundException if the marking item does not exist.
     * @throws MarkingItemNotForAssessmentException if the marking item is not for the given assessment.
     * @throws ForbiddenException if the user is not authorised to delete the marking item - they must be the module lecturer.
     * @throws RubricLockedException if the assessment is locked.
     */
    @Transactional
    public void deleteMarkingItem(Long assessmentId, Long markingItemId, AppUserDetails principal) {
        if (!assessmentRepository.existsByIdAndModule_Lecturer_Id(assessmentId, principal.getId())) {
            throw new ForbiddenException("You are not authorised to delete this marking item.");
        }

        MarkingItem item = markingItemRepository.findById(markingItemId)
                .orElseThrow(() -> new MarkingItemNotFoundException(markingItemId));
        if (!item.getAssessment().getId().equals(assessmentId)) {
            throw new MarkingItemNotForAssessmentException(markingItemId, assessmentId);
        }

        if (isRubricLocked(assessmentId)) {
            throw new RubricLockedException(assessmentId);
        }

        markingItemRepository.delete(item);
    }

    /**
     * Reorders the marking items for an assessment.
     * @param assessmentId the assessment id
     * @param orderedIds takes a list of all the assessment's marking items, in the order they should be displayed.
     * @param principal the authenticated user - taken from the security context.
     * @throws ForbiddenException if the user is not authorised to reorder the marking items - they must be the module lecturer.
     * @throws InvalidReorderException if the list of marking item ids does not match the assessment's marking items.
     */
    @Transactional
    public void reorderMarkingItems(Long assessmentId, List<Long> orderedIds, AppUserDetails principal) {
        if (!assessmentRepository.existsByIdAndModule_Lecturer_Id(assessmentId, principal.getId())) {
            throw new ForbiddenException("You are not authorised to reorder marking items.");
        }
        List<MarkingItem> items = markingItemRepository
                .findByAssessmentIdOrderByPosition(assessmentId);

        Set<Long> existingIds = items.stream().map(MarkingItem::getId).collect(toSet());
        if (!existingIds.equals(new HashSet<>(orderedIds)) || orderedIds.size() != items.size()) {
            throw new InvalidReorderException(assessmentId);
        }

        Map<Long, MarkingItem> byId = items.stream()
                .collect(toMap(MarkingItem::getId, i -> i));

        for (int i = 0; i < orderedIds.size(); i++) {
            byId.get(orderedIds.get(i)).setPosition((short) i);
        }
    }

    /**
     * Gets the assessment stats for a given lecturer.
     * @param principal the authenticated user - taken from the security context.
     * @return the assessment stats.
     */
    @Transactional(readOnly = true)
    public List<AssessmentStatsResponse> getAllAssessmentStatsByLecturer(AppUserDetails principal) {
        return assessmentRepository.findByLecturerId(principal.getId()).stream()
                .map(assessment -> getAssessmentStats(assessment, principal.getId()))
                .toList();
    }

    /**
     * Checks whether the assessment rubric is locked - i.e. if there are any feedbacks for the assessment.
     * This is to prevent existing feedback from being made invalid after changing the rubric.
     * @param assessmentId the assessment id
     * @return true if the rubric is locked, false otherwise.
     */
    private boolean isRubricLocked(Long assessmentId) {
        return feedbackRepository.existsByAssessmentId(assessmentId);
    }

    /**
     * Converts an assessment to an assessment response.
     * @param assessment the assessment to convert
     * @return the assessment response.
     */
    private AssessmentResponse assessmentToResponse(Assessment assessment, String academicYear) {
        List<MarkingItemResponse> markingItems = assessment.getMarkingItems().stream()
                .map(item -> markingItemToResponse(item, assessment.getId()))
                .toList();
        int totalMark = markingItems.stream().mapToInt(MarkingItemResponse::maxMark).sum();
        boolean isRubricLocked = feedbackRepository.existsByAssessmentId(assessment.getId());
        return new AssessmentResponse(assessment.getId(), assessment.getTitle(), assessment.getDueDate(), assessment.getModule().getId(), assessment.getModule().getTitle(), markingItems, totalMark, assessment.getWeight(), academicYear, assessment.getFeedbackDueDate(), isRubricLocked);
    }

    /**
     * Converts a marking item to a marking item response.
     * @param markingItem the marking item to convert
     * @param assessmentId the assessment id
     * @return the marking item response.
     */
    private MarkingItemResponse markingItemToResponse(MarkingItem markingItem, Long assessmentId) {
        return new MarkingItemResponse(markingItem.getId(), assessmentId, markingItem.getName(), markingItem.getMaxMark(), markingItem.getPosition());
    }

    /**
     * Gets the assessment stats for a given assessment and lecturer.
     * @param assessment the assessment
     * @param lecturerId the lecturer id
     * @return the assessment stats.
     */
    private AssessmentStatsResponse getAssessmentStats(Assessment assessment, Long lecturerId) {
        Long enrolled = enrolmentRepository.countByModuleId(assessment.getModule().getId());
        Long published = feedbackRepository.countByLecturerIdAndAssessmentIdAndStatus(lecturerId, assessment.getId(), FeedbackStatus.PUBLISHED);
        Long drafts = feedbackRepository.countByLecturerIdAndAssessmentIdAndStatus(lecturerId, assessment.getId(), FeedbackStatus.DRAFT);
        Long todo = Math.max(0, enrolled - published - drafts);
        return new AssessmentStatsResponse(assessment.getId(), enrolled, drafts, published, todo);
    }
}