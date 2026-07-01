package com.dissertation.backend.common;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ModuleRepository moduleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void seed() {
        if (userRepository.count() > 0) {
            return;   // idempotency guard — don't re-seed on every restart
        }

        AppUser admin = new AppUser();
        admin.setEmail("admin@dissertation.com");
        admin.setFullName("Admin User");
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setRole(UserRole.ADMIN);

        AppUser lecturer = new AppUser();
        lecturer.setEmail("lecturer@dissertation.com");
        lecturer.setFullName("Lecturer User");
        lecturer.setPasswordHash(passwordEncoder.encode("password123"));
        lecturer.setRole(UserRole.LECTURER);

        userRepository.saveAll(List.of(admin, lecturer));

        List<CourseModule> modules = moduleRepository.findAll();
        modules.forEach(module -> module.setLecturer(lecturer));
        moduleRepository.saveAll(modules);
    }
}