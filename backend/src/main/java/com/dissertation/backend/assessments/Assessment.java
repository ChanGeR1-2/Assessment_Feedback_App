package com.dissertation.backend.assessments;

import com.dissertation.backend.course_modules.CourseModule;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id",  nullable = false)
    private CourseModule module;

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

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }
}
