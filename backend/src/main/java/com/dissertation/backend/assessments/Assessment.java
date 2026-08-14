package com.dissertation.backend.assessments;

import com.dissertation.backend.course_modules.CourseModule;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessment")
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id",  nullable = false)
    private CourseModule module;
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<MarkingItem> markingItems = new ArrayList<>();
    @Column(name = "weight")
    private Short weight;
    @Column(name = "feedback_due_date")
    private LocalDateTime feedbackDueDate;

    public List<MarkingItem> getMarkingItems() { return markingItems; }

    protected Assessment() {}

    public Assessment(String title, LocalDateTime dueDate, CourseModule module, Short weight, LocalDateTime feedbackDueDate) {
        this.title = title;
        this.dueDate = dueDate;
        this.module = module;
        this.weight = weight;
        this.feedbackDueDate = feedbackDueDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getFeedbackDueDate() {
        return feedbackDueDate;
    }

    public void setFeedbackDueDate(LocalDateTime feedbackDueDate) {
        this.feedbackDueDate = feedbackDueDate;
    }

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }

    public Short getWeight() {
        return weight;
    }

    public void setWeight(Short weight) {
        this.weight = weight;
    }
}
