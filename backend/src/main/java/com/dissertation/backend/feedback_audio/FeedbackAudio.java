package com.dissertation.backend.feedback_audio;

import com.dissertation.backend.feedback.Feedback;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_audio")
public class FeedbackAudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    protected FeedbackAudio() {}

    public FeedbackAudio(Feedback feedback, String filename, String contentType, Long sizeBytes) {
        this.feedback = feedback;
        this.filename = filename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public Long getId() {
        return id;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public String getFilename() {
        return filename;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}