package com.dissertation.backend.feedback;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.assessments.Assessment;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "student_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser student;
    @JoinColumn(name = "lecturer_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser lecturer;
    @JoinColumn(name = "assessment_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Assessment assessment;
    @Column(name = "mark", nullable = false)
    private Short mark;
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackItem> items = new ArrayList<>();

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

    public Long getId() {
        return id;
    }

    public AppUser getStudent() {
        return student;
    }

    public void setStudent(AppUser student) {
        this.student = student;
    }

    public AppUser getLecturer() {
        return lecturer;
    }

    public void setLecturer(AppUser lecturer) {
        this.lecturer = lecturer;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Short getMark() {
        return mark;
    }

    public void setMark(Short mark) {
        this.mark = mark;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<FeedbackItem> getItems() { return items; }
}
