package com.dissertation.backend.common;

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
import com.dissertation.backend.feedback.FeedbackStatus;
import com.dissertation.backend.feedback_queries.FeedbackQuery;
import com.dissertation.backend.feedback_queries.FeedbackQueryAnswer;
import com.dissertation.backend.feedback_queries.FeedbackQueryAnswerRepository;
import com.dissertation.backend.feedback_queries.FeedbackQueryRepository;
import com.dissertation.backend.phrases.FeedbackPhrase;
import com.dissertation.backend.phrases.FeedbackPhraseRepository;
import com.dissertation.backend.tags.FeedbackTag;
import com.dissertation.backend.tags.Tag;
import com.dissertation.backend.tags.TagRepository;
import com.dissertation.backend.tags.TagType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder {

    private static final String DEFAULT_PASSWORD = "password123";

    private static final List<String> STUDENT_NAMES = List.of(
            "Amelia Hart", "Daniel Okafor", "Priya Raman", "Tom Wallace",
            "Sofia Bianchi", "James Whitfield", "Aisha Karim", "Ewan Docherty",
            "Grace Adeyemi", "Marcus Lindqvist", "Nadia Haddad", "Oliver Chen",
            "Ruth Kavanagh", "Samir Patel", "Lucy Brennan"
    );

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final AssessmentRepository assessmentRepository;
    private final MarkingItemRepository markingItemRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FeedbackRepository feedbackRepository;
    private final TagRepository tagRepository;
    private final FeedbackQueryRepository feedbackQueryRepository;
    private final FeedbackQueryAnswerRepository feedbackQueryAnswerRepository;
    private final FeedbackPhraseRepository feedbackPhraseRepository;

    public DataSeeder(UserRepository userRepository,
                      ModuleRepository moduleRepository,
                      AssessmentRepository assessmentRepository,
                      MarkingItemRepository markingItemRepository,
                      EnrolmentRepository enrolmentRepository,
                      PasswordEncoder passwordEncoder,
                      FeedbackRepository feedbackRepository,
                      TagRepository tagRepository,
                      FeedbackQueryRepository feedbackQueryRepository,
                      FeedbackQueryAnswerRepository feedbackQueryAnswerRepository,
                      FeedbackPhraseRepository feedbackPhraseRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.assessmentRepository = assessmentRepository;
        this.markingItemRepository = markingItemRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.feedbackRepository = feedbackRepository;
        this.tagRepository = tagRepository;
        this.feedbackQueryRepository = feedbackQueryRepository;
        this.feedbackQueryAnswerRepository = feedbackQueryAnswerRepository;
        this.feedbackPhraseRepository = feedbackPhraseRepository;
    }

    @Transactional
    public void seed() {
        List<CourseModule> modules = moduleRepository.findAll();
        if (modules.isEmpty()) {
            return;   // Flyway hasn't seeded modules; nothing to build on
        }

        AppUser lecturer = seedUsers();
        assignLecturer(modules, lecturer);
        seedMarkingItems();
        seedEnrolments(modules);
        seedFeedback();
        seedQueries();
        seedPhrases();
    }

    /** Creates the admin, lecturer and students. Returns the lecturer. */
    private AppUser seedUsers() {
        if (userRepository.count() > 0) {
            return userRepository.findByEmail("lecturer@dissertation.com").orElseThrow();
        }

        String passwordHash = passwordEncoder.encode(DEFAULT_PASSWORD);

        AppUser admin = buildUser("admin@dissertation.com", "Admin User", UserRole.ADMIN, passwordHash);
        AppUser lecturer = buildUser("lecturer@dissertation.com", "Rachel Doyle", UserRole.LECTURER, passwordHash);

        List<AppUser> users = new ArrayList<>();
        users.add(admin);
        users.add(lecturer);
        for (String name : STUDENT_NAMES) {
            users.add(buildUser(emailFor(name), name, UserRole.STUDENT, passwordHash));
        }

        userRepository.saveAll(users);
        return lecturer;
    }

    private void assignLecturer(List<CourseModule> modules, AppUser lecturer) {
        modules.stream()
                .filter(m -> m.getLecturer() == null)
                .forEach(m -> m.setLecturer(lecturer));
        moduleRepository.saveAll(modules);
    }

    /**
     * AS2 on each module uses question-style items with a non-100 total, to
     * exercise both the unified marking-item model and variable assessment
     * totals. The rest use typical coursework criteria.
     */
    private void seedMarkingItems() {
        if (markingItemRepository.count() > 0) {
            return;
        }

        List<MarkingItem> items = new ArrayList<>();

        for (Assessment assessment : assessmentRepository.findAll()) {
            if ("AS2".equals(assessment.getTitle())) {
                items.add(new MarkingItem(assessment, "Q1", (short) 15, (short) 0));
                items.add(new MarkingItem(assessment, "Q2", (short) 25, (short) 1));
                items.add(new MarkingItem(assessment, "Q3", (short) 20, (short) 2));
                items.add(new MarkingItem(assessment, "Q4", (short) 20, (short) 3));
            } else {
                items.add(new MarkingItem(assessment, "Critical analysis",      (short) 30, (short) 0));
                items.add(new MarkingItem(assessment, "Use of evidence",        (short) 25, (short) 1));
                items.add(new MarkingItem(assessment, "Structure and argument", (short) 25, (short) 2));
                items.add(new MarkingItem(assessment, "Referencing",            (short) 10, (short) 3));
                items.add(new MarkingItem(assessment, "Written expression",     (short) 10, (short) 4));
            }
        }

        markingItemRepository.saveAll(items);
    }

    /**
     * Everyone takes the first module; two thirds also take the second, so the
     * cohorts differ and some students build a longer feedback history.
     */
    private void seedEnrolments(List<CourseModule> modules) {
        if (enrolmentRepository.count() > 0) {
            return;
        }

        List<AppUser> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT)
                .toList();

        List<Enrolment> enrolments = new ArrayList<>();
        CourseModule primary = modules.get(0);

        for (int i = 0; i < students.size(); i++) {
            enrolments.add(new Enrolment(students.get(i), primary));
            if (modules.size() > 1 && i % 3 != 2) {
                enrolments.add(new Enrolment(students.get(i), modules.get(1)));
            }
        }

        enrolmentRepository.saveAll(enrolments);
    }

    private String emailFor(String fullName) {
        return fullName.toLowerCase().replace(" ", ".") + "@dissertation.com";
    }

    private AppUser buildUser(String email, String fullName, UserRole role, String passwordHash) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }

    /**
     * Marking coverage is deliberately uneven so the lecturer dashboard shows
     * work in progress: AS1 is fully marked and published, AS2 is partly marked
     * with some drafts, AS3 is untouched.
     *
     * Each student is given a persistent weakness and strength so that the tag
     * aggregation surfaces genuine recurring themes rather than uniform noise.
     */
    private void seedFeedback() {
        if (feedbackRepository.count() > 0) {
            return;
        }

        AppUser lecturer = userRepository.findByEmail("lecturer@dissertation.com").orElseThrow();
        List<Tag> tags = tagRepository.findAll();
        if (tags.isEmpty()) {
            return;   // tag vocabulary not seeded
        }

        List<Assessment> assessments = assessmentRepository.findAll();
        List<Feedback> toSave = new ArrayList<>();

        for (Assessment assessment : assessments) {
            if ("AS3".equals(assessment.getTitle())) {
                continue;   // left unmarked
            }

            List<MarkingItem> markingItems =
                    markingItemRepository.findByAssessmentIdOrderByPosition(assessment.getId());
            if (markingItems.isEmpty()) {
                continue;
            }

            List<AppUser> cohort = enrolmentRepository.findByModuleId(assessment.getModule().getId())
                    .stream().map(Enrolment::getStudent).toList();

            for (int i = 0; i < cohort.size(); i++) {
                AppUser student = cohort.get(i);

                // AS2: only the first two thirds are marked, and every third of
                // those is left as a draft.
                boolean isSecondAssessment = "AS2".equals(assessment.getTitle());
                if (isSecondAssessment && i >= (cohort.size() * 2) / 3) {
                    continue;
                }
                boolean publish = !(isSecondAssessment && i % 3 == 0);

                toSave.add(buildFeedback(student, lecturer, assessment, markingItems, tags, i, publish));
            }
        }

        feedbackRepository.saveAll(toSave);
    }

    private Feedback buildFeedback(AppUser student, AppUser lecturer, Assessment assessment,
                                   List<MarkingItem> markingItems, List<Tag> tags,
                                   int studentIndex, boolean publish) {

        // A stable per-student ability band (0 = struggling, 3 = strong) so a
        // student's marks are consistent across their assessments.
        int band = studentIndex % 4;

        // Nudge the band up on later assessments so students visibly improve.
        int assessmentIndex = "AS2".equals(assessment.getTitle()) ? 1 : 0;
        int variationSeed = assessment.getId().intValue();
        int effectiveBand = Math.min(3, band + assessmentIndex);

        Feedback feedback = new Feedback(student, lecturer, assessment, (short) 0,
                SUMMARIES.get(effectiveBand));

        short total = 0;
        for (int j = 0; j < markingItems.size(); j++) {
            MarkingItem item = markingItems.get(j);
            short awarded = awardedMarkFor(item, effectiveBand, studentIndex, variationSeed);
            feedback.addItem(new FeedbackItem(feedback, item, awarded,
                    commentFor(effectiveBand, studentIndex, j, variationSeed)));
            total += awarded;
        }
        feedback.setMark(total);

        // Persistent themes: each student keeps hitting the same weakness and
        // showing the same strength, so recurring patterns emerge over time.
        Tag weakness = tags.get(studentIndex % tags.size());
        Tag strength = tags.get((studentIndex + 3) % tags.size());
        if (!weakness.getId().equals(strength.getId())) {
            feedback.addTag(new FeedbackTag(weakness, feedback, TagType.IMPROVEMENT));
            feedback.addTag(new FeedbackTag(strength, feedback, TagType.STRENGTH));
        }

        if (publish) {
            feedback.publish();
        }
        return feedback;
    }

    private short awardedMarkFor(MarkingItem item, int band, int studentIndex, int variationSeed) {
        double baseRatio = switch (band) {
            case 0 -> 0.42;
            case 1 -> 0.58;
            case 2 -> 0.68;
            default -> 0.81;
        };
        double variation = ((studentIndex + item.getPosition() + variationSeed) % 5 - 2) * 0.05;
        double ratio = Math.clamp(baseRatio + variation, 0.1, 1.0);
        return (short) Math.round(item.getMaxMark() * ratio);
    }

    private static final List<String> SUMMARIES = List.of(
            "There is a reasonable attempt here, but the work needs more depth and clearer structure. Focus on developing your argument rather than describing the material.",
            "A solid piece of work that meets the brief. The analysis is sound but could go further in evaluating the evidence you present.",
            "A good piece of work with clear structure and confident use of sources. Push the critical evaluation a little further to reach the top band.",
            "An excellent submission. The argument is well developed and the evidence is used critically throughout. Very little to fault here."
    );

    /** Picks a comment that varies by criterion as well as by ability band. */
    private String commentFor(int band, int studentIndex, int itemIndex, int variationSeed) {
        List<String> pool = COMMENTS_BY_BAND.get(band);
        return pool.get((studentIndex + itemIndex + variationSeed) % pool.size());
    }

    private static final List<List<String>> COMMENTS_BY_BAND = List.of(
            List.of(
                    "This is mostly descriptive. Try to explain why the evidence matters, not just what it says.",
                    "The point here is difficult to follow — consider setting out your reasoning step by step.",
                    "There is relevant material, but it isn't yet connected to the question being asked.",
                    "Some of the claims here need supporting with a source.",
                    "Proofreading would help; several sentences are hard to parse."
            ),
            List.of(
                    "Competently handled, though the reasoning could be spelled out more explicitly.",
                    "A reasonable treatment. Consider what a counter-argument would look like.",
                    "The evidence is appropriate, but you stop short of evaluating it.",
                    "Clear enough, though the link back to your overall argument is implicit.",
                    "Generally accurate, with a few points that would benefit from expansion."
            ),
            List.of(
                    "Well argued, with only minor gaps in the supporting detail.",
                    "A confident treatment — the reasoning is easy to follow throughout.",
                    "Good use of sources here, and you engage with what they actually claim.",
                    "Clearly structured, and the point lands well.",
                    "Strong, though there is room to push the evaluation slightly further."
            ),
            List.of(
                    "Handled confidently, with a clear line of reasoning and well-chosen evidence.",
                    "Excellent — you engage critically rather than simply reporting.",
                    "The synthesis of sources here is genuinely impressive.",
                    "Precise and well supported throughout.",
                    "This section is a real strength of the submission."
            )
    );

    private void seedQueries() {
        if (feedbackQueryRepository.count() > 0) {
            return;
        }

        AppUser lecturer = userRepository.findByEmail("lecturer@dissertation.com").orElseThrow();

        // only published feedback can be queried
        List<Feedback> published = feedbackRepository.findAll().stream()
                .filter(f -> f.getStatus() == FeedbackStatus.PUBLISHED)
                .toList();

        List<FeedbackQuery> queries = new ArrayList<>();
        List<FeedbackQueryAnswer> answers = new ArrayList<>();

        for (int i = 0; i < published.size() && queries.size() < 6; i++) {
            if (i % 4 != 0) continue;   // spread them across the cohort

            Feedback feedback = published.get(i);
            FeedbackQuery query = new FeedbackQuery(
                    QUESTIONS.get(queries.size() % QUESTIONS.size()),
                    feedback,
                    feedback.getStudent());
            queries.add(query);

            // answer the first two, leave the rest pending
            if (queries.size() <= 2) {
                answers.add(new FeedbackQueryAnswer(
                        ANSWERS.get(queries.size() - 1), query, lecturer));
            }
        }

        feedbackQueryRepository.saveAll(queries);
        feedbackQueryAnswerRepository.saveAll(answers);
    }

    private static final List<String> QUESTIONS = List.of(
            "Thanks for the feedback. Could you say a bit more about what you meant by pushing the evaluation further? I'm not sure what that would look like in practice.",
            "I lost more marks on referencing than I expected — is it the format I'm getting wrong, or am I not citing enough?",
            "Could you clarify why the structure mark was lower than the analysis mark? I thought the two were quite closely linked in my essay.",
            "Is there any reading you'd recommend to help with the critical evaluation side before the next assessment?",
            "I'm unclear on what was missing in Q3 — I thought I'd covered the main points but only got half marks.",
            "Would it be possible to go over the argument development comment? I want to make sure I approach AS3 differently."
    );

    private static final List<String> ANSWERS = List.of(
            "Good question. At the moment you tend to present a source and move on — try adding a sentence after each one saying what it contributes to your argument, or where it falls short. That's the step that moves description into evaluation.",
            "It's mostly format — several of your in-text citations are missing page numbers, and two of the entries in your reference list don't match the citations. The volume of citation is fine."
    );

    private void seedPhrases() {
        if (feedbackPhraseRepository.count() > 0) return;

        AppUser lecturer = userRepository.findByEmail("lecturer@dissertation.com").orElseThrow();

        feedbackPhraseRepository.saveAll(List.of(
                new FeedbackPhrase(lecturer, "Descriptive not analytical",
                        "This section describes the material rather than analysing it. Try explaining why the evidence matters to your argument."),
                new FeedbackPhrase(lecturer, "Needs a source",
                        "This claim would be stronger with a supporting reference."),
                new FeedbackPhrase(lecturer, "Signposting",
                        "Consider signposting your argument more explicitly in the introduction so the reader knows where you're heading."),
                new FeedbackPhrase(lecturer, "Good use of evidence",
                        "You engage with what your sources actually claim rather than simply citing them, which works well here."),
                new FeedbackPhrase(lecturer, "Referencing format",
                        "Check your citation format against the module guide — page numbers are missing in several places."),
                new FeedbackPhrase(lecturer, "Counter-argument",
                        "Consider what someone disagreeing with you would say, and address it directly.")
        ));
    }
}