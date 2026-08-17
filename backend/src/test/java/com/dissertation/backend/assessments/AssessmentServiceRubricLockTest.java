package com.dissertation.backend.assessments;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.assessments.dto.CreateMarkingItemRequest;
import com.dissertation.backend.assessments.dto.EditMarkingItemRequest;
import com.dissertation.backend.assessments.dto.MarkingItemResponse;
import com.dissertation.backend.assessments.exceptions.MarkingItemNotForAssessmentException;
import com.dissertation.backend.assessments.exceptions.RubricLockedException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AssessmentService}, specifically the rubric lock functionality.
 * Marking items should not be able to be added, deleted, renamed, or edited in any other way
 * once the rubric is locked. (i.e. once feedback has been attached to the assessment).
 * Reordering is exempt, as position is presentational only.
 */
@ExtendWith(MockitoExtension.class)
class AssessmentServiceRubricLockTest {
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private AssessmentRepository assessmentRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private EnrolmentRepository enrolmentRepository;
    @Mock private MarkingItemRepository markingItemRepository;

    @InjectMocks
    private AssessmentService assessmentService;

    private static final Long STUDENT_ID = 1L;
    private static final Long LECTURER_ID = 2L;
    private static final Long ASSESSMENT_ID = 10L;
    private static final Long MODULE_ID = 20L;
    private static final Long ITEM_A_ID = 100L;
    private static final Long ITEM_B_ID = 101L;
    private static final Long OTHER_ASSESSMENT_ID = 11L;
    private static final Long OTHER_ITEM_ID = 102L;

    private Assessment otherAssessment;
    private MarkingItem otherItem;

    private AppUser student;
    private AppUser lecturer;
    private AppUserDetails principal;
    private CourseModule module;
    private Assessment assessment;
    private MarkingItem itemA;
    private MarkingItem itemB;

    @BeforeEach
    void setUp() {
        student = user(STUDENT_ID, "Amelia Hart", UserRole.STUDENT);
        lecturer = user(LECTURER_ID, "Rachel Doyle", UserRole.LECTURER);
        principal = new AppUserDetails(lecturer);

        module = new CourseModule();
        setId(module, MODULE_ID);
        module.setTitle("Introduction to Databases");
        module.setCode("CS101");
        module.setAcademicYear("2026/2027");
        module.setLecturer(lecturer);

        assessment = new Assessment("AS1", LocalDateTime.now().plusWeeks(2), module, (short) 30, LocalDateTime.now().plusWeeks(6));
        setId(assessment, ASSESSMENT_ID);
        otherAssessment = new Assessment("AS2", LocalDateTime.now().plusWeeks(4), module,
                (short) 30, LocalDateTime.now().plusWeeks(8));
        setId(otherAssessment, OTHER_ASSESSMENT_ID);

        otherItem = new MarkingItem(otherAssessment, "Structure", (short) 25, (short) 0);
        setId(otherItem, OTHER_ITEM_ID);

        itemA = new MarkingItem(assessment, "Critical analysis", (short) 20, (short) 0);
        setId(itemA, ITEM_A_ID);
        itemB = new MarkingItem(assessment, "Referencing", (short) 30, (short) 1);
        setId(itemB, ITEM_B_ID);
    }

    @Test
    @DisplayName("blocks adding a marking item once feedback exists")
    void blocksAddWhenFeedbackExists() {
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
        when(feedbackRepository.existsByAssessmentId(ASSESSMENT_ID)).thenReturn(true);

        assertThatThrownBy(() -> assessmentService.createMarkingItem(
                ASSESSMENT_ID, new CreateMarkingItemRequest("New criterion", (short) 10, (short) 2), principal))
                .isInstanceOf(RubricLockedException.class);
    }

    @Test
    @DisplayName("blocks editing a marking item once feedback exists")
    void blocksEditWhenFeedbackExists() {
        when(markingItemRepository.findById(ITEM_A_ID)).thenReturn(Optional.of(itemA));
        when(feedbackRepository.existsByAssessmentId(ASSESSMENT_ID)).thenReturn(true);

        assertThatThrownBy(() -> assessmentService.editMarkingItem(
                ASSESSMENT_ID, ITEM_A_ID, new EditMarkingItemRequest("Edited criterion", (short) 15), principal
        )).isInstanceOf(RubricLockedException.class);
    }

    @Test
    @DisplayName("blocks deleting a marking item once feedback exists")
    void blocksDeleteWhenFeedbackExists() {
        when(assessmentRepository.existsByIdAndModule_Lecturer_Id(ASSESSMENT_ID, lecturer.getId())).thenReturn(true);
        when(markingItemRepository.findById(ITEM_A_ID)).thenReturn(Optional.of(itemA));
        when(feedbackRepository.existsByAssessmentId(ASSESSMENT_ID)).thenReturn(true);

        assertThatThrownBy(() -> assessmentService.deleteMarkingItem(ASSESSMENT_ID, ITEM_A_ID, principal))
                .isInstanceOf(RubricLockedException.class);
    }

    @Test
    @DisplayName("allows adding a marking item if feedback does not exist")
    void allowsAddWhenFeedbackNotExists() {
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
        when(feedbackRepository.existsByAssessmentId(ASSESSMENT_ID)).thenReturn(false);
        when(markingItemRepository.save(any(MarkingItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarkingItemResponse response = assessmentService.createMarkingItem(
                ASSESSMENT_ID, new CreateMarkingItemRequest("New criterion", (short) 10, (short) 2), principal);

        assertThat(response.name()).isEqualTo("New criterion");
        assertThat(response.maxMark()).isEqualTo((short) 10);
        assertThat(response.position()).isEqualTo((short) 2);
    }

    @Test
    @DisplayName("allows reordering marking items even if feedback exists")
    void reorderAllowedWhenFeedbackExists() {
        when(assessmentRepository.existsByIdAndModule_Lecturer_Id(ASSESSMENT_ID, LECTURER_ID)).thenReturn(true);
        when(markingItemRepository.findByAssessmentIdOrderByPosition(ASSESSMENT_ID))
                .thenReturn(List.of(itemA, itemB));

        assessmentService.reorderMarkingItems(ASSESSMENT_ID, List.of(ITEM_B_ID, ITEM_A_ID), principal);

        assertThat(itemB.getPosition()).isEqualTo((short) 0);
        assertThat(itemA.getPosition()).isEqualTo((short) 1);
        verify(feedbackRepository, never()).existsByAssessmentId(any());
    }

    @Test
    @DisplayName("rejects editing a marking item that belongs to a different assessment")
    void rejectsEditOfItemFromAnotherAssessment() {
        when(markingItemRepository.findById(OTHER_ITEM_ID)).thenReturn(Optional.of(otherItem));

        assertThatThrownBy(() -> assessmentService.editMarkingItem(
                ASSESSMENT_ID, OTHER_ITEM_ID,
                new EditMarkingItemRequest("Renamed", (short) 15), principal))
                .isInstanceOf(MarkingItemNotForAssessmentException.class);
    }

    @Test
    @DisplayName("rejects deleting a marking item that belongs to a different assessment")
    void rejectsDeleteOfItemFromAnotherAssessment() {
        when(assessmentRepository.existsByIdAndModule_Lecturer_Id(ASSESSMENT_ID, LECTURER_ID)).thenReturn(true);
        when(markingItemRepository.findById(OTHER_ITEM_ID)).thenReturn(Optional.of(otherItem));

        assertThatThrownBy(() -> assessmentService.deleteMarkingItem(ASSESSMENT_ID, OTHER_ITEM_ID, principal))
                .isInstanceOf(MarkingItemNotForAssessmentException.class);

        verify(markingItemRepository, never()).delete(any());
    }

    private AppUser user(Long id, String name, UserRole role) {
        AppUser user = new AppUser();
        setId(user, id);
        user.setFullName(name);
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@dissertation.com");
        user.setRole(role);
        return user;
    }

    /** Entity ids are database-generated, so they must be set reflectively in tests. */
    private void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
