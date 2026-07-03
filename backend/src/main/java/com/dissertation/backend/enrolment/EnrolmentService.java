package com.dissertation.backend.enrolment;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.dto.UserResponse;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import com.dissertation.backend.enrolment.dto.CreateEnrolmentRequest;
import com.dissertation.backend.enrolment.dto.EnrolmentResponse;
import com.dissertation.backend.enrolment.exceptions.EnrolmentExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrolmentService {
    private final EnrolmentRepository enrolmentRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    public EnrolmentService(EnrolmentRepository enrolmentRepository, ModuleRepository moduleRepository, UserRepository userRepository) {
        this.enrolmentRepository = enrolmentRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EnrolmentResponse enrol(CreateEnrolmentRequest request) {
        if (enrolmentRepository.existsByStudentIdAndModuleId(request.studentId(), request.moduleId())) {
            throw new EnrolmentExistsException(request.studentId(), request.moduleId());
        }
        CourseModule module = moduleRepository.findById(request.moduleId()).orElseThrow(() -> new ModuleNotFoundException(request.moduleId()));

        AppUser user = userRepository.findById(request.studentId()).orElseThrow(() -> new UserNotFoundException(request.studentId()));

        Enrolment enrolment = new Enrolment(user, module);
        Enrolment saved = enrolmentRepository.save(enrolment);
        return new EnrolmentResponse(saved.getId(), user.getId(), module.getId(), saved.getEnrolledAt());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getEnrolledStudents(Long moduleId) {
        return enrolmentRepository.findByModuleId(moduleId).stream()
                .map(Enrolment::getStudent)
                .map(s -> new UserResponse(s.getId(), s.getFullName(), s.getEmail(), s.getRole()))
                .toList();
    }
}
