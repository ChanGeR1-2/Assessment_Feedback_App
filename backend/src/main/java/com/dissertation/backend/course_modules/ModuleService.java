package com.dissertation.backend.course_modules;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.course_modules.dto.AssignModuleRequest;
import com.dissertation.backend.course_modules.dto.CreateModuleRequest;
import com.dissertation.backend.course_modules.dto.ModuleResponse;
import com.dissertation.backend.course_modules.exceptions.ModuleExistsException;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleService {
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    public ModuleService(ModuleRepository moduleRepository, UserRepository userRepository) {
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
    }

    public List<ModuleResponse> getAllModules() {
        return moduleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ModuleResponse createModule(CreateModuleRequest request) {
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

    @Transactional
    public ModuleResponse assignModuleLecturer(AssignModuleRequest request, Long moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(ModuleNotFoundException::new);
        AppUser lecturer = userRepository.findById(request.lecturerId())
                .orElseThrow(() -> new UserNotFoundException(request.lecturerId()));
        module.setLecturer(lecturer);
        return toResponse(moduleRepository.save(module));
    }

    private ModuleResponse toResponse(CourseModule module) {
        return new ModuleResponse(module.getId(), module.getTitle(), module.getCode(), module.getAcademicYear(), module.getLecturer() != null ? module.getLecturer().getId() : null);
    }
}
