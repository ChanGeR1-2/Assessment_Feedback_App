package com.dissertation.backend.tags;

import com.dissertation.backend.feedback.Feedback;
import jakarta.persistence.*;

@Entity
@Table(
        name = "feedback_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feedback_tag",
                columnNames = {"feedback_id", "tag_id"}
        )
)
public class FeedbackTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;
    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false)
    private TagType tagType;

    protected FeedbackTag() {}

    public FeedbackTag(Tag tag, Feedback feedback, TagType tagType) {
        this.tag = tag;
        this.feedback = feedback;
        this.tagType = tagType;
    }
    public Long getId() {
        return id;
    }
    public Tag getTag() {
        return tag;
    }
    public Feedback getFeedback() {
        return feedback;
    }
    public TagType getTagType() {
        return tagType;
    }
}
