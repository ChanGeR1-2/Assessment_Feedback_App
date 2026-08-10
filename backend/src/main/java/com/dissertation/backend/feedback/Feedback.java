package com.dissertation.backend.feedback;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.assessments.Assessment;
import com.dissertation.backend.tags.FeedbackTag;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a feedback submission by a student for an assessment.
 *
 * <p> Feedback is immutable once it has been published.
 *
 * <p> Marking items and tags are stored as sets rather than lists, to avoid
 * MultipleBagFetchException and duplicate rows from the cartesian product when both collections are fetch-joined
 */
@Entity
@Table(
        name = "feedback",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feedback_assessment_student",
                columnNames = {"assessment_id", "student_id"}
        )
)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private AppUser lecturer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    @Column(name = "mark", nullable = false)
    private Short mark;
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status = FeedbackStatus.DRAFT;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeedbackItem> items = new LinkedHashSet<>();
    // Set rather than List: avoids MultipleBagFetchException when both
    // collections are fetch-joined in the same query.
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeedbackTag> tags = new LinkedHashSet<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    protected Feedback() {}

    public Feedback(AppUser student, AppUser lecturer, Assessment assessment, Short mark, String summary) {
        this.student = student;
        this.lecturer = lecturer;
        this.assessment = assessment;
        this.mark = mark;
        this.summary = summary;
    }

    public void addItem(FeedbackItem item) {
        items.add(item);
    }

    public void addTag(FeedbackTag tag) {
        tags.add(tag);
    }

    public void publish() {
        this.status = FeedbackStatus.PUBLISHED;
    }

    public Long getId() { return id; }

    public AppUser getStudent() { return student; }

    public AppUser getLecturer() { return lecturer; }

    public Assessment getAssessment() { return assessment; }

    public Short getMark() { return mark; }

    public void setMark(Short mark) { this.mark = mark; }

    public String getSummary() { return summary; }

    public void setSummary(String summary) { this.summary = summary; }

    public FeedbackStatus getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Set<FeedbackItem> getItems() { return items; }

    public Set<FeedbackTag> getTags() { return tags; }
}