package com.dissertation.backend.assessments;

import jakarta.persistence.*;

@Entity
@Table(
        name = "marking_item",
        uniqueConstraints = @UniqueConstraint(
                name="uk_marking_item_assessment_name",
                columnNames = {"assessment_id", "name"}
        )
)
public class MarkingItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "max_mark", nullable = false)
    private Short maxMark;
    @Column(name = "position", nullable = false)
    private Short position;

    protected MarkingItem() {}

    public MarkingItem(Assessment assessment, String name, Short maxMark, Short position) {
        this.assessment = assessment;
        this.name = name;
        this.maxMark = maxMark;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Short getPosition() {
        return position;
    }

    public void setPosition(Short position) {
        this.position = position;
    }

    public Short getMaxMark() {
        return maxMark;
    }

    public void setMaxMark(Short maxMark) {
        this.maxMark = maxMark;
    }
}
