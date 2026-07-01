package com.dissertation.backend.enrolment;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.course_modules.CourseModule;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrolment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrolment_student_module",
                columnNames = {"student_id", "module_id"}
        )
)
public class Enrolment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @PrePersist
    void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
    }

    protected Enrolment() {}

    public Enrolment(AppUser student, CourseModule module) {
        this.student = student;
        this.module = module;
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

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
