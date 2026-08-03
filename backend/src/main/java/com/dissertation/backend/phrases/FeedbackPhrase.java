package com.dissertation.backend.phrases;

import com.dissertation.backend.app_users.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_phrase")
public class FeedbackPhrase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private AppUser lecturer;
    @Column(name = "label", nullable = false)
    private String label;
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    protected FeedbackPhrase() {}

    public FeedbackPhrase(AppUser lecturer, String label, String text) {
        this.lecturer = lecturer;
        this.label = label;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public AppUser getLecturer() {
        return lecturer;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
