package com.dissertation.backend.feedback;

import com.dissertation.backend.testsupport.TestDataBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests for {@link FeedbackRepository}, chosen to prevent issues with duplication of records when two
 * collections are fetched in the same query (e.g. {@link FeedbackRepository#findByIdWithDetails(Long)}).
 * The tests are not exhaustive, but should cover the most important cases.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeedbackRepositoryTest {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired private TestDataBuilder data;

    @PersistenceContext private EntityManager entityManager;

    @Test
    void findByIdWithDetailsDoesNotDuplicateItems() {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(scenario.student(), scenario.lecturer(),
                scenario.assessment(), scenario.markingItems());

        entityManager.flush();
        entityManager.clear();

        var found = feedbackRepository.findByIdWithDetails(feedback.getId()).orElseThrow();

        assertThat(found.getItems()).hasSize(2);
    }

    @Test
    void findByStudentIdDoesNotReturnDrafts() {
        var scenario = data.scenario();
        var publishedFeedback = data.publishedFeedback(scenario.student(), scenario.lecturer(),
                scenario.assessment(), scenario.markingItems());
        var assessment = data.assessment(scenario.module());
        var draftFeedback = data.draftFeedback(scenario.student(), scenario.lecturer(),
                assessment, data.markingItems(assessment));

        entityManager.flush();
        entityManager.clear();

        var found = feedbackRepository.findByStudentId(scenario.student().getId(), FeedbackStatus.PUBLISHED);

        assertThat(found)
                .extracting(Feedback::getId)
                .containsExactly(publishedFeedback.getId())
                .doesNotContain(draftFeedback.getId());
    }

    @Test
    void findByIdWithDetailsDoesNotDuplicateItemsOrTags() {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(scenario.student(), scenario.lecturer(),
                scenario.assessment(), scenario.markingItems());
        data.withTags(feedback, data.someTags(3));

        entityManager.flush();
        entityManager.clear();

        var found = feedbackRepository.findByIdWithDetails(feedback.getId()).orElseThrow();

        assertThat(found.getItems()).hasSize(2);
        assertThat(found.getTags()).hasSize(3);
    }

    @Test
    void findByIdWithDetailsFetchesEverythingTheMapperNeeds() {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(scenario.student(), scenario.lecturer(),
                scenario.assessment(), scenario.markingItems());
        data.withTags(feedback, data.someTags(2));

        entityManager.flush();
        entityManager.clear();

        var found = feedbackRepository.findByIdWithDetails(feedback.getId()).orElseThrow();

        entityManager.detach(found);

        assertThatCode(() -> {
            found.getStudent().getFullName();
            found.getLecturer().getFullName();
            found.getAssessment().getModule().getTitle();
            found.getItems().forEach(i -> i.getMarkingItem().getName());
            found.getTags().forEach(t -> t.getTag().getName());
        }).doesNotThrowAnyException();
    }

    @Test
    void countByLecturerIdAndAssessmentIdAndStatusReturnsCorrectCounts() {
        var scenario = data.scenario();
        var items = scenario.markingItems();

        data.publishedFeedback(scenario.student(), scenario.lecturer(), scenario.assessment(), items);
        data.publishedFeedback(data.student(), scenario.lecturer(), scenario.assessment(), items);
        data.draftFeedback(data.student(), scenario.lecturer(), scenario.assessment(), items);

        var publishedCount = feedbackRepository.countByLecturerIdAndAssessmentIdAndStatus(
                scenario.lecturer().getId(), scenario.assessment().getId(), FeedbackStatus.PUBLISHED);
        var draftCount = feedbackRepository.countByLecturerIdAndAssessmentIdAndStatus(
                scenario.lecturer().getId(), scenario.assessment().getId(), FeedbackStatus.DRAFT);

        assertThat(publishedCount).isEqualTo(2);
        assertThat(draftCount).isEqualTo(1);
    }
}
