package com.dissertation.backend.feedback_queries;

import com.dissertation.backend.app_users.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_query_answer",
        uniqueConstraints = @UniqueConstraint(
                name="uk_answer_query",
                columnNames = {"feedback_query_id"}
        )
)
public class FeedbackQueryAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "answer", nullable = false)
    private String answer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_query_id", nullable = false)
    private FeedbackQuery feedbackQuery;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private AppUser lecturer;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    protected FeedbackQueryAnswer() {}

    public FeedbackQueryAnswer(String answer, FeedbackQuery feedbackQuery, AppUser lecturer) {
        this.answer = answer;
        this.feedbackQuery = feedbackQuery;
        this.lecturer = lecturer;
    }

    public Long getId() {
        return id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String response) {
        this.answer = response;
    }

    public FeedbackQuery getFeedbackQuery() {
        return feedbackQuery;
    }

    public AppUser getLecturer() {
        return lecturer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
