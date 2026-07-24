package com.dissertation.backend.enrolment;

import com.dissertation.backend.app_users.dto.UserResponse;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.course_modules.dto.ModuleResponse;
import com.dissertation.backend.enrolment.dto.CreateEnrolmentRequest;
import com.dissertation.backend.enrolment.dto.EnrolmentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EnrolmentController {
    private final EnrolmentService enrolmentService;
    public EnrolmentController(EnrolmentService enrolmentService) {
        this.enrolmentService = enrolmentService;
    }

    @GetMapping(path="/enrolments")
    public ResponseEntity<List<UserResponse>> getEnrolledStudents(@RequestParam(required = false) Long moduleId, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(enrolmentService.getEnrolledStudents(principal.getId(), moduleId));
    }

    @PostMapping(path = "/enrolments")
    public ResponseEntity<EnrolmentResponse> enrol(@Valid @RequestBody CreateEnrolmentRequest request, @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(enrolmentService.enrol(request, principal), HttpStatus.CREATED);
    }

    @GetMapping(path="/students/{studentId}/modules")
    public ResponseEntity<List<ModuleResponse>> getEnrolledModules(@PathVariable Long studentId, @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(enrolmentService.getEnrolledModules(studentId, principal));
    }

}
