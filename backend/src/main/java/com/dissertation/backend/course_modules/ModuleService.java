package com.dissertation.backend.course_modules;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.course_modules.dto.AssignModuleRequest;
import com.dissertation.backend.course_modules.dto.CreateModuleRequest;
import com.dissertation.backend.course_modules.dto.ModuleResponse;
import com.dissertation.backend.course_modules.exceptions.ModuleExistsException;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleService {
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final EnrolmentRepository enrolmentRepository;
    public ModuleService(ModuleRepository moduleRepository, UserRepository userRepository, EnrolmentRepository enrolmentRepository) {
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.enrolmentRepository = enrolmentRepository;
    }

    @Transactional(readOnly = true)
    public List<ModuleResponse> getAllModulesByLecturerId(Long lecturerId) {
        return moduleRepository.findByLecturerId(lecturerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ModuleResponse createModule(CreateModuleRequest request, AppUserDetails principal) {
        if (principal.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can create modules.");
        }
        if (moduleRepository.existsByCodeAndAcademicYear(request.code(), request.academicYear())) {
            throw new ModuleExistsException(request.code(), request.academicYear());
        }

        CourseModule module = new CourseModule();
        module.setTitle(request.title());
        module.setCode(request.code());
        module.setAcademicYear(request.academicYear());

        if (request.lecturerId() != null) {
            AppUser lecturer = userRepository.findById(request.lecturerId())
                    .orElseThrow(() -> new UserNotFoundException(request.lecturerId()));
            module.setLecturer(lecturer);
        }

        CourseModule savedModule = moduleRepository.save(module);
        return toResponse(savedModule);
    }

    @Transactional(readOnly = true)
    public ModuleResponse getModuleById(Long id, AppUserDetails principal) {
        CourseModule module =  moduleRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundException(id));
        return switch (principal.getRole()) {
            case ADMIN -> toResponse(module);
            case LECTURER -> {
                if (module.getLecturer() == null
                        || !module.getLecturer().getId().equals(principal.getId())) {
                    throw new ForbiddenException("You are not authorised to view this module.");
                }
                yield toResponse(module);
            }
            case STUDENT -> {
                if (!enrolmentRepository.existsByStudentIdAndModuleId(principal.getId(), id)) {
                    throw new ForbiddenException("You are not authorised to view this module.");
                }
                yield toResponse(module);
            }
            default -> throw new ForbiddenException("You are not authorised to view this module.");
        };
    }

    @Transactional
    public ModuleResponse assignModuleLecturer(AssignModuleRequest request, Long moduleId, AppUserDetails principal) {
        if (principal.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can assign lecturers to modules.");
        }
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
        AppUser lecturer = userRepository.findById(request.lecturerId())
                .orElseThrow(() -> new UserNotFoundException(request.lecturerId()));
        module.setLecturer(lecturer);
        return toResponse(moduleRepository.save(module));
    }

    private ModuleResponse toResponse(CourseModule module) {
        return new ModuleResponse(module.getId(), module.getTitle(), module.getCode(), module.getAcademicYear(), module.getLecturer() != null ? module.getLecturer().getId() : null, module.getLecturer() != null ? module.getLecturer().getFullName() : null);
    }
}
