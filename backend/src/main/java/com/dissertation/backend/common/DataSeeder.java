package com.dissertation.backend.common;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import com.dissertation.backend.enrolment.Enrolment;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder {

    private static final int STUDENT_COUNT = 50;
    private static final String DEFAULT_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ModuleRepository moduleRepository,
                      EnrolmentRepository enrolmentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void seed() {
        if (userRepository.count() > 0) {
            return;   // idempotency guard — don't re-seed on every restart
        }

        String passwordHash = passwordEncoder.encode(DEFAULT_PASSWORD);

        AppUser admin = buildUser("admin@dissertation.com", "Admin User", UserRole.ADMIN, passwordHash);
        AppUser lecturer = buildUser("lecturer@dissertation.com", "Lecturer User", UserRole.LECTURER, passwordHash);

        List<AppUser> students = new ArrayList<>();
        for (int i = 1; i <= STUDENT_COUNT; i++) {
            students.add(buildUser(
                    "student" + i + "@dissertation.com",
                    "Student " + i,
                    UserRole.STUDENT,
                    passwordHash));
        }

        List<AppUser> allUsers = new ArrayList<>();
        allUsers.add(admin);
        allUsers.add(lecturer);
        allUsers.addAll(students);
        userRepository.saveAll(allUsers);

        List<CourseModule> modules = moduleRepository.findAll();
        if (modules.isEmpty()) {
            return;   // nothing to enrol into; Flyway seed didn't create modules
        }
        modules.forEach(module -> module.setLecturer(lecturer));
        moduleRepository.saveAll(modules);

        List<Enrolment> enrolments = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            AppUser student = students.get(i);

            CourseModule primary = modules.get(i % modules.size());
            enrolments.add(new Enrolment(student, primary));

            if (modules.size() > 1 && i % 3 == 0) {
                CourseModule secondary = modules.get((i + 1) % modules.size());
                enrolments.add(new Enrolment(student, secondary));
            }
        }
        enrolmentRepository.saveAll(enrolments);
    }

    private AppUser buildUser(String email, String fullName, UserRole role, String passwordHash) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }
}