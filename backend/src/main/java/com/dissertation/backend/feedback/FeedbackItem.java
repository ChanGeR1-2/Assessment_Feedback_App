package com.dissertation.backend.feedback;

import com.dissertation.backend.assessments.MarkingItem;
import jakarta.persistence.*;

@Entity
@Table
        (name = "feedback_item",
                uniqueConstraints = @UniqueConstraint(
                        name="uk_feedback_item_feedback_marking_item",
                        columnNames = {"feedback_id", "marking_item_id"}
                )
        )
public class FeedbackItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marking_item_id", nullable = false)
    private MarkingItem markingItem;
    @Column(name = "awarded_mark", nullable = false)
    private Short awardedMark;
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    protected FeedbackItem() {}

    public FeedbackItem(Feedback feedback, MarkingItem markingItem, Short awardedMark, String comment) {
        this.feedback = feedback;
        this.markingItem = markingItem;
        this.awardedMark = awardedMark;
        this.comment = comment;
    }
    public Long getId() {
        return id;
    }
    public Feedback getFeedback() {
        return feedback;
    }
    public MarkingItem getMarkingItem() {
        return markingItem;
    }
    public Short getAwardedMark() {
        return awardedMark;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setAwardedMark(Short awardedMark) {
        this.awardedMark = awardedMark;
    }
    public void setMarkingItem(MarkingItem markingItem) {
        this.markingItem = markingItem;
    }
    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

}
