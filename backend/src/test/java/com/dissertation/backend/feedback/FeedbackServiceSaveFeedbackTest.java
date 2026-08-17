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
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.dto.CreateFeedbackItemRequest;
import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import com.dissertation.backend.feedback.exceptions.*;
import com.dissertation.backend.tags.FeedbackTag;
import com.dissertation.backend.tags.Tag;
import com.dissertation.backend.tags.TagRepository;
import com.dissertation.backend.tags.TagType;
import com.dissertation.backend.tags.dto.CreateFeedbackTagRequest;
import com.dissertation.backend.tags.dto.FeedbackTagResponse;
import com.dissertation.backend.tags.exceptions.DuplicateTagException;
import com.dissertation.backend.tags.exceptions.TagNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FeedbackService#saveFeedback}, the most heavily
 * validated operation in the system. Repositories are mocked so that each
 * validation rule can be exercised in isolation.
 */
@ExtendWith(MockitoExtension.class)
class FeedbackServiceSaveFeedbackTest {

    @Mock private FeedbackRepository feedbackRepository;
    @Mock private AssessmentRepository assessmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private EnrolmentRepository enrolmentRepository;
    @Mock private MarkingItemRepository markingItemRepository;
    @Mock private TagRepository tagRepository;

    @InjectMocks private FeedbackService feedbackService;

    private static final Long STUDENT_ID = 1L;
    private static final Long LECTURER_ID = 2L;
    private static final Long ASSESSMENT_ID = 10L;
    private static final Long MODULE_ID = 20L;
    private static final Long ITEM_A_ID = 100L;
    private static final Long ITEM_B_ID = 101L;
    private static final Long TAG_REFERENCING_ID = 200L;
    private static final Long TAG_CRITICAL_ANALYSIS_ID = 201L;

    private AppUser student;
    private AppUser lecturer;
    private CourseModule module;
    private Assessment assessment;
    private MarkingItem itemA;   // max 20
    private MarkingItem itemB;   // max 30
    private Tag tagReferencing;
    private Tag tagCriticalAnalysis;

    @BeforeEach
    void setUp() {
        student = user(STUDENT_ID, "Amelia Hart", UserRole.STUDENT);
        lecturer = user(LECTURER_ID, "Rachel Doyle", UserRole.LECTURER);

        module = new CourseModule();
        setId(module, MODULE_ID);
        module.setTitle("Introduction to Databases");
        module.setCode("CS101");
        module.setAcademicYear("2026/2027");
        module.setLecturer(lecturer);

        assessment = new Assessment("AS1", LocalDateTime.now().plusWeeks(2), module, (short) 30, LocalDateTime.now().plusWeeks(6));
        setId(assessment, ASSESSMENT_ID);

        itemA = new MarkingItem(assessment, "Critical analysis", (short) 20, (short) 0);
        setId(itemA, ITEM_A_ID);
        itemB = new MarkingItem(assessment, "Referencing", (short) 30, (short) 1);
        setId(itemB, ITEM_B_ID);

        tagReferencing = new Tag("Referencing");
        setId(tagReferencing, TAG_REFERENCING_ID);
        tagCriticalAnalysis = new Tag("Critical Analysis");
        setId(tagCriticalAnalysis, TAG_CRITICAL_ANALYSIS_ID);
    }

    // ---------------------------------------------------------------- success

    @Nested
    @DisplayName("valid submissions")
    class ValidSubmissions {

        @Test
        @DisplayName("saves feedback with the mark derived from the item marks")
        void savesFeedbackWithDerivedMark() {
            stubHappyPath();

            FeedbackResponse response = feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 15), item(ITEM_B_ID, 24)), LECTURER_ID, false);

            // 15 + 24 = 39, out of 20 + 30 = 50
            assertThat(response.mark()).isEqualTo((short) 39);
            assertThat(response.totalMark()).isEqualTo(50);
            assertThat(response.items()).hasSize(2);
        }

        @Test
        @DisplayName("saves as a draft when publish is false")
        void savesAsDraft() {
            stubHappyPath();

            FeedbackResponse response = feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10), item(ITEM_B_ID, 10)), LECTURER_ID, false);

            assertThat(response.status()).isEqualTo(FeedbackStatus.DRAFT);
        }

        @Test
        @DisplayName("publishes immediately when publish is true")
        void publishesImmediately() {
            stubHappyPath();

            FeedbackResponse response = feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10), item(ITEM_B_ID, 10)), LECTURER_ID, true);

            assertThat(response.status()).isEqualTo(FeedbackStatus.PUBLISHED);
        }

        @Test
        @DisplayName("accepts a mark equal to the criterion maximum")
        void acceptsMarkAtMaximum() {
            stubHappyPath();

            FeedbackResponse response = feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 20), item(ITEM_B_ID, 30)), LECTURER_ID, false);

            assertThat(response.mark()).isEqualTo((short) 50);
        }
    }

    // ------------------------------------------------------------ tags

    @Nested
    @DisplayName("tag validation")
    class TagValidation {

        @Test
        @DisplayName("saves feedback with tags")
        void savesFeedbackWithTags() {
            when(tagRepository.findAllById(any())).thenReturn(List.of(tagReferencing, tagCriticalAnalysis));
            stubHappyPath();

            FeedbackResponse response = feedbackService.saveFeedback(
                    request(List.of(tag(TAG_REFERENCING_ID, TagType.STRENGTH), tag(TAG_CRITICAL_ANALYSIS_ID, TagType.IMPROVEMENT)),
                            item(ITEM_A_ID, 15), item(ITEM_B_ID, 24)), LECTURER_ID, false
            );

            assertThat(response.tags()).hasSize(2);
            assertThat(response.tags())
                    .extracting(FeedbackTagResponse::tagId, FeedbackTagResponse::tagType)
                    .containsExactlyInAnyOrder(
                            tuple(TAG_REFERENCING_ID, TagType.STRENGTH),
                            tuple(TAG_CRITICAL_ANALYSIS_ID, TagType.IMPROVEMENT));
        }

        @Test
        @DisplayName("saves feedback with no tags")
        void savesFeedbackWithoutTags() {
            when(tagRepository.findAllById(any())).thenReturn(List.of());
            stubHappyPath();

            FeedbackResponse response = feedbackService.saveFeedback(
                    request((List<CreateFeedbackTagRequest>) null, item(ITEM_A_ID, 15), item(ITEM_B_ID, 24)), LECTURER_ID, false
            );

            assertThat(response.tags()).isEmpty();
        }

        @Test
        @DisplayName("rejects duplicate tags")
        void rejectsDuplicateTags() {
            stubValidationPath();

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(List.of(tag(TAG_REFERENCING_ID, TagType.STRENGTH), tag(TAG_REFERENCING_ID, TagType.IMPROVEMENT)),
                            item(ITEM_A_ID, 15), item(ITEM_B_ID, 24)), LECTURER_ID, false
            )).isInstanceOf(DuplicateTagException.class);
        }

        @Test
        @DisplayName("rejects unknown tags")
        void rejectsUnknownTags() {
            when(tagRepository.findAllById(any())).thenReturn(List.of(tagReferencing));
            stubValidationPath();

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(List.of(tag(99L, TagType.STRENGTH), tag(TAG_CRITICAL_ANALYSIS_ID, TagType.IMPROVEMENT)),
                            item(ITEM_A_ID, 15), item(ITEM_B_ID, 24)), LECTURER_ID, false
            )).isInstanceOf(TagNotFoundException.class);
        }
    }


    // ------------------------------------------------------------ participants

    @Nested
    @DisplayName("participant validation")
    class ParticipantValidation {

        @Test
        @DisplayName("rejects a student id that does not exist")
        void rejectsUnknownStudent() {
            when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("rejects a lecturer that does not exist")
        void rejectsUnknownLecturer() {
            when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
            when(userRepository.findAppUserById(LECTURER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("rejects a student id belonging to a non-student")
        void rejectsNonStudent() {
            AppUser notAStudent = user(STUDENT_ID, "Someone Else", UserRole.LECTURER);
            when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.of(notAStudent));

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(InvalidRoleException.class);
        }

        @Test
        @DisplayName("rejects an assessment that does not exist")
        void rejectsUnknownAssessment() {
            when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(AssessmentNotFoundException.class);
        }

        @Test
        @DisplayName("rejects a lecturer id belonging to a non-lecturer")
        void rejectsNonLecturer() {
            AppUser notALecturer = user(LECTURER_ID, "Another Student", UserRole.STUDENT);
            when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(userRepository.findAppUserById(LECTURER_ID)).thenReturn(Optional.of(notALecturer));
            when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(InvalidRoleException.class);
        }
    }

    // ----------------------------------------------------------- authorisation

    @Nested
    @DisplayName("authorisation")
    class Authorisation {

        @Test
        @DisplayName("rejects a lecturer who does not own the module")
        void rejectsNonOwningLecturer() {
            AppUser otherLecturer = user(99L, "Other Lecturer", UserRole.LECTURER);
            module.setLecturer(otherLecturer);

            when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(userRepository.findAppUserById(LECTURER_ID)).thenReturn(Optional.of(lecturer));
            when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("rejects an unassigned module without throwing NPE")
        void rejectsModuleWithNoLecturer() {
            module.setLecturer(null);

            when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(userRepository.findAppUserById(LECTURER_ID)).thenReturn(Optional.of(lecturer));
            when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // --------------------------------------------------------- preconditions

    @Nested
    @DisplayName("preconditions")
    class Preconditions {

        @Test
        @DisplayName("rejects a second piece of feedback for the same student and assessment")
        void rejectsDuplicateFeedback() {
            stubParticipants();
            when(feedbackRepository.existsByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(FeedbackExistsException.class);
        }

        @Test
        @DisplayName("rejects a student who is not enrolled on the module")
        void rejectsUnenrolledStudent() {
            stubParticipants();
            when(feedbackRepository.existsByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                    .thenReturn(false);
            when(enrolmentRepository.existsByStudentIdAndModuleId(STUDENT_ID, MODULE_ID))
                    .thenReturn(false);

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(StudentNotEnrolledException.class);
        }
    }

    // ------------------------------------------------------------ marking items

    @Nested
    @DisplayName("marking item validation")
    class MarkingItemValidation {

        @Test
        @DisplayName("rejects the same marking item submitted twice")
        void rejectsDuplicateMarkingItem() {
            stubValidationPath();

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10), item(ITEM_A_ID, 12)), LECTURER_ID, false))
                    .isInstanceOf(DuplicateMarkingItemException.class);
        }

        @Test
        @DisplayName("rejects feedback that does not cover every marking item")
        void rejectsIncompleteFeedback() {
            stubValidationPath();

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(IncompleteFeedbackException.class);
        }

        @Test
        @DisplayName("rejects a mark above the criterion maximum")
        void rejectsMarkAboveMaximum() {
            stubValidationPath();

            assertThatThrownBy(() -> feedbackService.saveFeedback(
                    request(item(ITEM_A_ID, 21), item(ITEM_B_ID, 10)), LECTURER_ID, false))
                    .isInstanceOf(InvalidMarkException.class);
        }
    }

    // -------------------------------------------------------------- helpers

    /** Stubs the participant lookups only — for tests that fail before the rubric is read. */
    private void stubParticipants() {
        when(userRepository.findAppUserById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(userRepository.findAppUserById(LECTURER_ID)).thenReturn(Optional.of(lecturer));
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    }

    /** Stubs validation dependencies — enough to reach the item-building stage. */
    private void stubValidationPath() {
        stubParticipants();
        when(feedbackRepository.existsByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(false);
        when(enrolmentRepository.existsByStudentIdAndModuleId(STUDENT_ID, MODULE_ID))
                .thenReturn(true);
        when(markingItemRepository.findByAssessmentIdOrderByPosition(ASSESSMENT_ID))
                .thenReturn(List.of(itemA, itemB));
    }

    /** As above, plus a save that returns its argument — for tests that expect success. */
    private void stubHappyPath() {
        stubValidationPath();
        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private CreateFeedbackRequest request(CreateFeedbackItemRequest... items) {
        return request(List.of(), items);
    }

    private CreateFeedbackRequest request(List<CreateFeedbackTagRequest> tags, CreateFeedbackItemRequest... items) {
        return new CreateFeedbackRequest(ASSESSMENT_ID, STUDENT_ID, "A summary of the work.",
                List.of(items), tags);
    }

    private CreateFeedbackItemRequest item(Long markingItemId, int awardedMark) {
        return new CreateFeedbackItemRequest(markingItemId, (short) awardedMark, "A comment.");
    }

    private CreateFeedbackTagRequest tag(Long tagId, TagType type) {
        return new CreateFeedbackTagRequest(tagId, type);
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