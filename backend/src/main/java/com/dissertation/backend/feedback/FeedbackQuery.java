package com.dissertation.backend.feedback;

import com.dissertation.backend.app_users.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_query",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_query_feedback",
                columnNames = {"feedback_id"}))
public class FeedbackQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query", nullable = false, columnDefinition = "TEXT")
    private String query;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    protected FeedbackQuery() {}

    public FeedbackQuery(String query, Feedback feedback, AppUser student) {
        this.query = query;
        this.feedback = feedback;
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public AppUser getStudent() {
        return student;
    }
}