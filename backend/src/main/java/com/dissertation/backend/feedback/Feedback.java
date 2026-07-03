package com.dissertation.backend.feedback;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.assessments.Assessment;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;
    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;
    @Column(name = "actions", columnDefinition = "TEXT")
    private String actions;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    protected Feedback() {}

    public Feedback(AppUser student, AppUser lecturer, Assessment assessment, Short mark, String strengths, String improvements, String actions) {
        this.student = student;
        this.lecturer = lecturer;
        this.assessment = assessment;
        this.mark = mark;
        this.strengths = strengths;
        this.improvements = improvements;
        this.actions = actions;
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

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getImprovements() {
        return improvements;
    }

    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
