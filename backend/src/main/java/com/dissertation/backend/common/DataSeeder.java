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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Populates the database with demonstration data under the {@code dev} profile.
 *
 * <p>Each phase guards on its own table, so new phases can be added and re-run
 * without wiping the database. The data is deliberately shaped rather than
 * random: marking coverage is uneven so the lecturer dashboard shows work in
 * progress, tags recur per student so the aggregation views surface genuine
 * patterns, and one student carries three years of history so cross-year
 * progress can be demonstrated.
 */
@Component
public class DataSeeder {

    private static final String DEFAULT_PASSWORD = "password123";
    private static final String LECTURER_EMAIL = "lecturer@dissertation.com";
    private static final String CURRENT_YEAR = "2026/2027";

    private static final List<String> STUDENT_NAMES = List.of(
            "Amelia Hart", "Daniel Okafor", "Priya Raman", "Tom Wallace",
            "Sofia Bianchi", "James Whitfield", "Aisha Karim", "Ewan Docherty",
            "Grace Adeyemi", "Marcus Lindqvist", "Nadia Haddad", "Oliver Chen",
            "Ruth Kavanagh", "Samir Patel", "Lucy Brennan"
    );

    /** Student with a multi-year history, used to demonstrate progress over time. */
    private static final String PROGRESSION_STUDENT_NAME = "Elena Marsh";

    // Tag names must match the seeded vocabulary exactly.
    private static final String FADING_WEAKNESS = "Referencing";
    private static final String FADING_WEAKNESS_2 = "Clarity of writing";
    private static final String PERSISTENT_WEAKNESS = "Critical analysis";
    private static final String GROWING_STRENGTH = "Structure";
    private static final String GROWING_STRENGTH_2 = "Use of evidence";
    private static final String CONSISTENT_STRENGTH = "Depth of research";

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
        seedProgressionStudent();   // before queries, so their feedback can be queried
        seedQueries();
        seedPhrases();
    }

    /** Creates the admin, lecturer, cohort and progression student. Returns the lecturer. */
    private AppUser seedUsers() {
        if (userRepository.count() > 0) {
            return userRepository.findByEmail(LECTURER_EMAIL).orElseThrow();
        }

        String passwordHash = passwordEncoder.encode(DEFAULT_PASSWORD);

        AppUser admin = buildUser("admin@dissertation.com", "Admin User", UserRole.ADMIN, passwordHash);
        AppUser lecturer = buildUser(LECTURER_EMAIL, "Rachel Doyle", UserRole.LECTURER, passwordHash);

        List<AppUser> users = new ArrayList<>();
        users.add(admin);
        users.add(lecturer);
        for (String name : STUDENT_NAMES) {
            users.add(buildUser(emailFor(name), name, UserRole.STUDENT, passwordHash));
        }
        users.add(buildUser(emailFor(PROGRESSION_STUDENT_NAME), PROGRESSION_STUDENT_NAME,
                UserRole.STUDENT, passwordHash));

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
     * AS2 on each assessment uses question-style items totalling 80, exercising
     * both the unified marking-item model and assessments whose total is not 100.
     * The rest use typical coursework criteria.
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
     * Enrols the cohort on the current year's modules only; earlier years exist
     * solely for the progression student. Everyone takes the first module and
     * two thirds also take the second, so the cohorts differ.
     */
    private void seedEnrolments(List<CourseModule> modules) {
        if (enrolmentRepository.count() > 0) {
            return;
        }

        List<CourseModule> currentModules = modules.stream()
                .filter(m -> CURRENT_YEAR.equals(m.getAcademicYear()))
                .toList();
        if (currentModules.isEmpty()) {
            return;
        }

        List<AppUser> students = cohortStudents();
        List<Enrolment> enrolments = new ArrayList<>();
        CourseModule primary = currentModules.get(0);

        for (int i = 0; i < students.size(); i++) {
            enrolments.add(new Enrolment(students.get(i), primary));
            if (currentModules.size() > 1 && i % 3 != 2) {
                enrolments.add(new Enrolment(students.get(i), currentModules.get(1)));
            }
        }

        enrolmentRepository.saveAll(enrolments);
    }

    /**
     * Marking coverage is deliberately uneven: AS1 is fully marked and published,
     * AS2 is marked for two thirds of the cohort with some left as drafts, and
     * AS3 is untouched — so the lecturer dashboard shows work in progress.
     *
     * <p>Each student keeps the same weakness and strength across their feedback,
     * so the tag aggregation surfaces recurring themes rather than noise.
     */
    private void seedFeedback() {
        if (feedbackRepository.count() > 0) {
            return;
        }

        AppUser lecturer = userRepository.findByEmail(LECTURER_EMAIL).orElseThrow();
        List<Tag> tags = tagRepository.findAll();
        if (tags.isEmpty()) {
            return;   // tag vocabulary not seeded
        }

        List<Feedback> toSave = new ArrayList<>();

        for (Assessment assessment : assessmentRepository.findAll()) {
            if ("AS3".equals(assessment.getTitle())) {
                continue;   // left unmarked
            }

            List<MarkingItem> markingItems =
                    markingItemRepository.findByAssessmentIdOrderByPosition(assessment.getId());
            if (markingItems.isEmpty()) {
                continue;
            }

            // The progression student's feedback is seeded separately, and earlier
            // years have no cohort enrolments, so this yields the current cohort only.
            List<AppUser> cohort = enrolmentRepository.findByModuleId(assessment.getModule().getId())
                    .stream()
                    .map(Enrolment::getStudent)
                    .filter(s -> !PROGRESSION_STUDENT_NAME.equals(s.getFullName()))
                    .toList();

            for (int i = 0; i < cohort.size(); i++) {
                boolean isSecondAssessment = "AS2".equals(assessment.getTitle());
                if (isSecondAssessment && i >= (cohort.size() * 2) / 3) {
                    continue;   // later students not yet marked
                }
                boolean publish = !(isSecondAssessment && i % 3 == 0);

                toSave.add(buildFeedback(cohort.get(i), lecturer, assessment, markingItems, tags, i, publish));
            }
        }

        feedbackRepository.saveAll(toSave);
    }

    private Feedback buildFeedback(AppUser student, AppUser lecturer, Assessment assessment,
                                   List<MarkingItem> markingItems, List<Tag> tags,
                                   int studentIndex, boolean publish) {

        // Stable per-student ability band (0 = struggling, 3 = strong).
        int band = studentIndex % 4;
        // Nudge the band up on later assessments so students visibly improve.
        int assessmentIndex = "AS2".equals(assessment.getTitle()) ? 1 : 0;
        int effectiveBand = Math.min(3, band + assessmentIndex);
        // Distinguishes assessments so a student's feedback differs between them.
        int variationSeed = assessment.getId().intValue();

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

    /**
     * Seeds one student with three years of history so that cross-year progress
     * can be demonstrated: marks climb each year, "Referencing" and "Clarity of
     * writing" are progressively addressed, "Critical analysis" persists, and
     * two strengths emerge in later years alongside one that is consistent.
     */
    private void seedProgressionStudent() {
        AppUser student = userRepository.findByEmail(emailFor(PROGRESSION_STUDENT_NAME)).orElse(null);
        if (student == null || !enrolmentRepository.findByStudentId(student.getId()).isEmpty()) {
            return;
        }

        AppUser lecturer = userRepository.findByEmail(LECTURER_EMAIL).orElseThrow();
        Map<String, Tag> tagsByName = tagRepository.findAll().stream()
                .collect(Collectors.toMap(Tag::getName, t -> t));

        List<CourseModule> modules = moduleRepository.findAll();

        List<Enrolment> enrolments = modules.stream()
                .map(m -> new Enrolment(student, m))
                .toList();
        enrolmentRepository.saveAll(enrolments);

        List<Feedback> feedbacks = new ArrayList<>();

        for (CourseModule module : modules) {
            int yearIndex = switch (module.getAcademicYear()) {
                case "2024/2025" -> 0;
                case "2025/2026" -> 1;
                default -> 2;
            };
            boolean currentYear = CURRENT_YEAR.equals(module.getAcademicYear());

            for (Assessment assessment : assessmentRepository.findByModuleId(module.getId())) {
                // AS3 of the current year is unmarked, as for everyone else
                if (currentYear && "AS3".equals(assessment.getTitle())) {
                    continue;
                }

                List<MarkingItem> markingItems =
                        markingItemRepository.findByAssessmentIdOrderByPosition(assessment.getId());
                if (markingItems.isEmpty()) {
                    continue;
                }

                feedbacks.add(buildProgressionFeedback(
                        student, lecturer, assessment, markingItems, tagsByName, yearIndex));
            }
        }

        feedbackRepository.saveAll(feedbacks);
    }

    private Feedback buildProgressionFeedback(AppUser student, AppUser lecturer, Assessment assessment,
                                              List<MarkingItem> markingItems,
                                              Map<String, Tag> tagsByName, int yearIndex) {

        // Marks climb across the three years: roughly 48% → 61% → 74%
        double baseRatio = 0.48 + (yearIndex * 0.13);
        int band = Math.min(3, yearIndex + 1);

        Feedback feedback = new Feedback(student, lecturer, assessment, (short) 0, SUMMARIES.get(band));

        short total = 0;
        for (int j = 0; j < markingItems.size(); j++) {
            MarkingItem item = markingItems.get(j);
            double variation = ((j + yearIndex) % 5 - 2) * 0.04;
            double ratio = Math.clamp(baseRatio + variation, 0.1, 1.0);
            short awarded = (short) Math.round(item.getMaxMark() * ratio);

            feedback.addItem(new FeedbackItem(feedback, item, awarded,
                    commentFor(band, yearIndex, j, assessment.getId().intValue())));
            total += awarded;
        }
        feedback.setMark(total);

        // Referencing: flagged throughout year 1, occasionally in year 2, gone by year 3.
        if (yearIndex == 0 || (yearIndex == 1 && assessment.getId() % 2 == 0)) {
            addTagIfPresent(feedback, tagsByName, FADING_WEAKNESS, TagType.IMPROVEMENT);
        }
        // Clarity of writing: a year 1 issue only.
        if (yearIndex == 0) {
            addTagIfPresent(feedback, tagsByName, FADING_WEAKNESS_2, TagType.IMPROVEMENT);
        }
        // Critical analysis: never resolved.
        addTagIfPresent(feedback, tagsByName, PERSISTENT_WEAKNESS, TagType.IMPROVEMENT);
        // Structure: recognised from year 2.
        if (yearIndex >= 1) {
            addTagIfPresent(feedback, tagsByName, GROWING_STRENGTH, TagType.STRENGTH);
        }
        // Use of evidence: recognised from year 3.
        if (yearIndex >= 2) {
            addTagIfPresent(feedback, tagsByName, GROWING_STRENGTH_2, TagType.STRENGTH);
        }
        // Depth of research: consistent throughout.
        addTagIfPresent(feedback, tagsByName, CONSISTENT_STRENGTH, TagType.STRENGTH);

        feedback.publish();
        return feedback;
    }

    private void addTagIfPresent(Feedback feedback, Map<String, Tag> tagsByName,
                                 String name, TagType type) {
        Tag tag = tagsByName.get(name);
        if (tag != null) {
            feedback.addTag(new FeedbackTag(tag, feedback, type));
        }
    }

    /** Seeds a handful of student questions, two of which have been answered. */
    private void seedQueries() {
        if (feedbackQueryRepository.count() > 0) {
            return;
        }

        AppUser lecturer = userRepository.findByEmail(LECTURER_EMAIL).orElseThrow();

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

            if (queries.size() <= 2) {
                answers.add(new FeedbackQueryAnswer(
                        ANSWERS.get(queries.size() - 1), query, lecturer));
            }
        }

        feedbackQueryRepository.saveAll(queries);
        feedbackQueryAnswerRepository.saveAll(answers);
    }

    /** Gives the lecturer a starting phrase bank so the picker is populated. */
    private void seedPhrases() {
        if (feedbackPhraseRepository.count() > 0) {
            return;
        }

        AppUser lecturer = userRepository.findByEmail(LECTURER_EMAIL).orElseThrow();

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

    private List<AppUser> cohortStudents() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT)
                .filter(u -> !PROGRESSION_STUDENT_NAME.equals(u.getFullName()))
                .toList();
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

    /** Picks a comment that varies by criterion as well as by ability band. */
    private String commentFor(int band, int studentIndex, int itemIndex, int variationSeed) {
        List<String> pool = COMMENTS_BY_BAND.get(band);
        return pool.get((studentIndex + itemIndex + variationSeed) % pool.size());
    }

    private static final List<String> SUMMARIES = List.of(
            "There is a reasonable attempt here, but the work needs more depth and clearer structure. Focus on developing your argument rather than describing the material.",
            "A solid piece of work that meets the brief. The analysis is sound but could go further in evaluating the evidence you present.",
            "A good piece of work with clear structure and confident use of sources. Push the critical evaluation a little further to reach the top band.",
            "An excellent submission. The argument is well developed and the evidence is used critically throughout. Very little to fault here."
    );

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
}