package com.dissertation.backend.course_modules;

import com.dissertation.backend.app_users.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "course_module", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_module_code_academic_year",
                columnNames = {"code", "academic_year"}
        )
})
public class CourseModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "academic_year", nullable = false)
    private String academicYear;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private AppUser moduleOwner;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public AppUser getModuleOwner() {
        return moduleOwner;
    }

    public void setModuleOwner(AppUser moduleOwner) {
        this.moduleOwner = moduleOwner;
    }
}
