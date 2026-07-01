package com.dissertation.backend.course_modules;

import com.dissertation.backend.course_modules.dto.AssignModuleRequest;
import com.dissertation.backend.course_modules.dto.CreateModuleRequest;
import com.dissertation.backend.course_modules.dto.ModuleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {
    private final ModuleService moduleService;
    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public ResponseEntity<List<ModuleResponse>> getAllModules() {
        return ResponseEntity.ok(moduleService.getAllModules());
    }

    @PostMapping
    public ResponseEntity<ModuleResponse> createModule(@Valid @RequestBody CreateModuleRequest request) {
        ModuleResponse module = moduleService.createModule(request);
        return new ResponseEntity<>(module, HttpStatus.CREATED);
    }

    @PatchMapping("/{moduleId}/lecturer")
    public ResponseEntity<ModuleResponse> assignLecturer(@PathVariable Long moduleId, @RequestBody AssignModuleRequest request) {
        return ResponseEntity.ok(moduleService.assignModuleLecturer(request, moduleId));
    }
}
