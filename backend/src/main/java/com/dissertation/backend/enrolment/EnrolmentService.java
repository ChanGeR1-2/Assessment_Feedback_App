package com.dissertation.backend.enrolment;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.app_users.dto.UserResponse;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.common.exceptions.InvalidRoleException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.course_modules.ModuleRepository;
import com.dissertation.backend.course_modules.dto.ModuleResponse;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import com.dissertation.backend.enrolment.dto.CreateEnrolmentRequest;
import com.dissertation.backend.enrolment.dto.EnrolmentResponse;
import com.dissertation.backend.enrolment.exceptions.EnrolmentExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    public EnrolmentResponse enrol(CreateEnrolmentRequest request, AppUserDetails userDetails) {
        if (userDetails.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can enrol students.");
        }
        if (enrolmentRepository.existsByStudentIdAndModuleId(request.studentId(), request.moduleId())) {
            throw new EnrolmentExistsException(request.studentId(), request.moduleId());
        }
        CourseModule module = moduleRepository.findById(request.moduleId()).orElseThrow(() -> new ModuleNotFoundException(request.moduleId()));

        AppUser user = userRepository.findById(request.studentId()).orElseThrow(() -> new UserNotFoundException(request.studentId()));

        if (user.getRole() != UserRole.STUDENT) {
            throw new InvalidRoleException(user.getId(), UserRole.STUDENT);
        }

        Enrolment enrolment = new Enrolment(user, module);
        Enrolment saved = enrolmentRepository.save(enrolment);
        return new EnrolmentResponse(saved.getId(), user.getId(), module.getId(), saved.getEnrolledAt());
    }

    private List<UserResponse> toStudentResponses(List<Enrolment> enrolments) {
        return enrolments.stream()
                .map(Enrolment::getStudent)
                .collect(Collectors.toMap(AppUser::getId, s -> s, (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .map(s -> new UserResponse(s.getId(), s.getFullName(), s.getEmail(), s.getRole()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<UserResponse> getEnrolledStudents(Long lecturerId, Long moduleId) {
        return toStudentResponses(enrolmentRepository.findByLecturerId(lecturerId, moduleId));
    }

    @Transactional(readOnly = true)
    public List<ModuleResponse> getEnrolledModules(Long studentId, AppUserDetails userDetails) {
        if (userDetails.getRole() == UserRole.STUDENT && !userDetails.getId().equals(studentId)) {
            throw new ForbiddenException("You are not authorised to view this list of modules.");
        }

        return toModuleResponses(enrolmentRepository.findByStudentId(studentId));
    }

    private List<ModuleResponse> toModuleResponses(List<Enrolment> enrolments) {
        return enrolments.stream()
                .map(Enrolment::getModule)
                .collect(Collectors.toMap(CourseModule::getId, m -> m, (a, b) -> a))
                .values().stream()
                .map(m -> {
                    AppUser lecturer = m.getLecturer();
                    return new ModuleResponse(m.getId(), m.getTitle(), m.getCode(), m.getAcademicYear(),
                            lecturer != null ? lecturer.getId() : null,
                            lecturer != null ? lecturer.getFullName() : null);
                })
                .toList();
    }

}
