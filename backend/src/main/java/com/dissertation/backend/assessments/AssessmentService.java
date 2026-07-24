package com.dissertation.backend.assessments;

import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.assessments.dto.*;
import com.dissertation.backend.assessments.exceptions.*;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

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

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAllAssessments(AppUserDetails principal) {
        return switch (principal.getRole()) {
            case ADMIN -> assessmentRepository.findAll().stream()
                    .map(this::assessmentToResponse)
                    .toList();
            case LECTURER -> assessmentRepository.findByLecturerId(principal.getId()).stream()
                    .map(this::assessmentToResponse)
                    .toList();
            case STUDENT -> assessmentRepository.findByStudentId(principal.getId()).stream()
                    .map(this::assessmentToResponse)
                    .toList();
        };
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAssessmentsByModuleId(Long moduleId, AppUserDetails principal) {
        return switch (principal.getRole()) {
            case ADMIN -> assessmentRepository.findByModuleId(moduleId).stream()
                    .map(this::assessmentToResponse)
                    .toList();
            case LECTURER -> {
                moduleRepository.findById(moduleId).ifPresent((module) -> {
                    if (module.getLecturer() == null || !module.getLecturer().getId().equals(principal.getId())) {
                        throw new ForbiddenException("You are not authorised to view this module.");
                    }
                });
                yield assessmentRepository.findByModuleId(moduleId).stream()
                        .map(this::assessmentToResponse)
                        .toList();

            }
            case STUDENT -> {
                if (!enrolmentRepository.existsByStudentIdAndModuleId(principal.getId(), moduleId)) {
                    throw new ForbiddenException("You are not authorised to view this module.");
                }
                yield assessmentRepository.findByModuleId(moduleId).stream()
                        .map(this::assessmentToResponse)
                        .toList();
            }
        };
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long id, AppUserDetails principal) {
        return switch (principal.getRole()) {
            case ADMIN -> assessmentRepository.findById(id)
                    .map(this::assessmentToResponse)
                    .orElseThrow(() -> new AssessmentNotFoundException(id));
            case LECTURER -> {
                Assessment assessment = assessmentRepository.findById(id)
                        .orElseThrow(() -> new AssessmentNotFoundException(id));
                if (assessment.getModule().getLecturer() == null || !assessment.getModule().getLecturer().getId().equals(principal.getId())) {
                    throw new ForbiddenException("You are not authorised to view this assessment.");
                }
                yield assessmentToResponse(assessment);
            }
            case STUDENT -> {
                Assessment assessment = assessmentRepository.findById(id)
                        .orElseThrow(() -> new AssessmentNotFoundException(id));
                if (!enrolmentRepository.existsByStudentIdAndModuleId(principal.getId(), assessment.getModule().getId())) {
                    throw new ForbiddenException("You are not authorised to view this assessment.");
                }
                yield assessmentToResponse(assessment);
            }
        };
    }

    @Transactional
    public AssessmentResponse createAssessment(CreateAssessmentRequest request, AppUserDetails principal) {
        if (principal.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can create assessments.");
        }
        CourseModule module = moduleRepository.findById(request.moduleId())
                .orElseThrow(() -> new InvalidModuleException(request.moduleId()));

        Assessment assessment = new Assessment(request.title(), request.dueDate(), module);
        return assessmentToResponse(assessmentRepository.save(assessment));
    }

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

    @Transactional
    public MarkingItemResponse editMarkingItem(Long assessmentId, Long markingItemId, EditMarkingItemRequest request, AppUserDetails principal) {
        MarkingItem item = markingItemRepository.findById(markingItemId)
                .orElseThrow(() -> new MarkingItemNotFoundException(markingItemId));

        if (item.getAssessment().getModule().getLecturer() == null || !item.getAssessment().getModule().getLecturer().getId().equals(principal.getId())) {
            throw new ForbiddenException("You are not authorised to edit this marking item.");
        }

        if (isRubricLocked(assessmentId)) {
            throw new RubricLockedException(assessmentId);
        }
        item.setName(request.name());
        item.setMaxMark(request.maxMark());
        return markingItemToResponse(markingItemRepository.save(item), assessmentId);
    }

    @Transactional
    public void deleteMarkingItem(Long assessmentId, Long markingItemId, AppUserDetails principal) {
        if (!assessmentRepository.existsByIdAndModule_Lecturer_Id(assessmentId, principal.getId())) {
            throw new ForbiddenException("You are not authorised to delete this marking item.");
        }
        if (isRubricLocked(assessmentId)) {
            throw new RubricLockedException(assessmentId);
        }
        markingItemRepository.deleteById(markingItemId);
    }

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

    private boolean isRubricLocked(Long assessmentId) {
        return feedbackRepository.existsByAssessmentId(assessmentId);
    }

    private AssessmentResponse assessmentToResponse(Assessment assessment) {
        List<MarkingItemResponse> markingItems = assessment.getMarkingItems().stream()
                .map(item -> markingItemToResponse(item, assessment.getId()))
                .toList();
        int totalMark = markingItems.stream().mapToInt(MarkingItemResponse::maxMark).sum();
        return new AssessmentResponse(assessment.getId(), assessment.getTitle(), assessment.getDueDate(), assessment.getModule().getId(), markingItems, totalMark);
    }

    private MarkingItemResponse markingItemToResponse(MarkingItem markingItem, Long assessmentId) {
        return new MarkingItemResponse(markingItem.getId(), assessmentId, markingItem.getName(), markingItem.getMaxMark(), markingItem.getPosition());
    }
}