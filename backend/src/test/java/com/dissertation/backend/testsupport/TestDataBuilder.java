package com.dissertation.backend.testsupport;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.assessments.Assessment;
import com.dissertation.backend.assessments.AssessmentRepository;
import com.dissertation.backend.assessments.MarkingItem;
import com.dissertation.backend.assessments.MarkingItemRepository;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import com.dissertation.backend.enrolment.Enrolment;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.Feedback;
import com.dissertation.backend.feedback.FeedbackItem;
import com.dissertation.backend.feedback.FeedbackRepository;
import com.dissertation.backend.tags.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates persisted fixtures for integration tests.
 *
 * <p>The Flyway migrations seed modules, assessments and the tag vocabulary,
 * but users, enrolments, marking items and feedback come from the {@code dev}
 * profile seeder, which does not run under test. Tests therefore create
 * whatever people and marking data they need through this builder.
 *
 * <p>Emails, assessment titles, and module codes are made unique per call so that fixtures created
 * by different tests cannot collide on unique constraints.
 */
@Component
public class TestDataBuilder {

    public static final String PASSWORD = "password123";

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final AssessmentRepository assessmentRepository;
    private final MarkingItemRepository markingItemRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagRepository tagRepository;
    private final FeedbackTagRepository feedbackTagRepository;

    private final AtomicInteger sequence = new AtomicInteger();

    public TestDataBuilder(UserRepository userRepository,
                           ModuleRepository moduleRepository,
                           AssessmentRepository assessmentRepository,
                           MarkingItemRepository markingItemRepository,
                           EnrolmentRepository enrolmentRepository,
                           FeedbackRepository feedbackRepository,
                           PasswordEncoder passwordEncoder,
                           TagRepository tagRepository,
                           FeedbackTagRepository feedbackTagRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.assessmentRepository = assessmentRepository;
        this.markingItemRepository = markingItemRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
        this.tagRepository = tagRepository;
        this.feedbackTagRepository = feedbackTagRepository;
    }

    // ------------------------------------------------------------------ users

    public AppUser student() {
        return user("Test Student", UserRole.STUDENT);
    }

    public AppUser lecturer() {
        return user("Test Lecturer", UserRole.LECTURER);
    }

    public AppUser admin() {
        return user("Test Admin", UserRole.ADMIN);
    }

    /** All test users share {@link #PASSWORD}, so any of them can be logged in. */
    public AppUser user(String name, UserRole role) {
        int n = sequence.incrementAndGet();
        AppUser user = new AppUser();
        user.setFullName(name + " " + n);
        user.setEmail("test.user." + n + "@dissertation.com");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        return userRepository.save(user);
    }

    // ---------------------------------------------------------------- modules

    public CourseModule module(AppUser lecturer) {
        int n = sequence.incrementAndGet();
        CourseModule module = new CourseModule();
        module.setTitle("Test Module " + n);
        module.setCode("TST" + n);
        module.setAcademicYear("2026/2027");
        module.setLecturer(lecturer);
        return moduleRepository.save(module);
    }

    public Enrolment enrol(AppUser student, CourseModule module) {
        return enrolmentRepository.save(new Enrolment(student, module));
    }

    // ------------------------------------------------------------ assessments

    public Assessment assessment(CourseModule module) {
        int n = sequence.incrementAndGet();
        Assessment assessment = new Assessment(
                "Test Assessment " + n,
                LocalDateTime.now().plusWeeks(2),
                module,
                (short) 40,
                LocalDateTime.now().plusWeeks(6));
        return assessmentRepository.save(assessment);
    }

    /** Two criteria worth 20 and 30, so an assessment totals 50. */
    public List<MarkingItem> markingItems(Assessment assessment) {
        return markingItemRepository.saveAll(List.of(
                new MarkingItem(assessment, "Critical analysis", (short) 20, (short) 0),
                new MarkingItem(assessment, "Referencing", (short) 30, (short) 1)));
    }

    // --------------------------------------------------------------- feedback

    /** Published feedback covering every marking item on the assessment. */
    public Feedback publishedFeedback(AppUser student, AppUser lecturer,
                                      Assessment assessment, List<MarkingItem> items) {
        Feedback feedback = buildFeedback(student, lecturer, assessment, items);
        feedback.publish();
        return feedbackRepository.save(feedback);
    }

    /** Draft feedback — not visible to the student. */
    public Feedback draftFeedback(AppUser student, AppUser lecturer,
                                  Assessment assessment, List<MarkingItem> items) {
        return feedbackRepository.save(buildFeedback(student, lecturer, assessment, items));
    }

    private Feedback buildFeedback(AppUser student, AppUser lecturer,
                                   Assessment assessment, List<MarkingItem> items) {
        Feedback feedback = new Feedback(student, lecturer, assessment, (short) 0,
                "A summary of the submitted work.");
        short total = 0;
        for (MarkingItem item : items) {
            short awarded = (short) (item.getMaxMark() / 2);
            feedback.addItem(new FeedbackItem(feedback, item, awarded, "A comment."));
            total += awarded;
        }
        feedback.setMark(total);
        return feedback;
    }

    // ------------------------------------------------------------------- tags

    /**
     * The tag vocabulary is seeded by Flyway, so tags are looked up rather than
     * created — creating one would collide with the unique name constraint.
     */
    public List<Tag> someTags(int count) {
        List<Tag> all = tagRepository.findAll();
        if (all.size() < count) {
            throw new IllegalStateException(
                    "Only " + all.size() + " seeded tags available, needed " + count);
        }
        return all.subList(0, count);
    }

    /** Attaches tags to feedback, alternating between strengths and improvements. */
    public Feedback withTags(Feedback feedback, List<Tag> tags) {
        for (int i = 0; i < tags.size(); i++) {
            TagType type = i % 2 == 0 ? TagType.STRENGTH : TagType.IMPROVEMENT;
            feedback.addTag(new FeedbackTag(tags.get(i), feedback, type));
        }
        return feedbackRepository.save(feedback);
    }

    // ------------------------------------------------------------- shorthands

    /**
     * A lecturer owning a module, a student enrolled on it, an assessment with
     * two marking items — the usual starting point for a feedback test.
     */
    public Scenario scenario() {
        AppUser lecturer = lecturer();
        AppUser student = student();
        CourseModule module = module(lecturer);
        enrol(student, module);
        Assessment assessment = assessment(module);
        List<MarkingItem> items = markingItems(assessment);
        return new Scenario(lecturer, student, module, assessment, items);
    }

    public record Scenario(AppUser lecturer,
                           AppUser student,
                           CourseModule module,
                           Assessment assessment,
                           List<MarkingItem> markingItems) {}
}