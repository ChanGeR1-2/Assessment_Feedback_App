package com.dissertation.backend.enrolment;

import com.dissertation.backend.app_users.dto.UserResponse;
import com.dissertation.backend.course_modules.dto.ModuleResponse;
import com.dissertation.backend.enrolment.dto.CreateEnrolmentRequest;
import com.dissertation.backend.enrolment.dto.EnrolmentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EnrolmentController {
    private final EnrolmentService enrolmentService;
    public EnrolmentController(EnrolmentService enrolmentService) {
        this.enrolmentService = enrolmentService;
    }


    // TODO: Implement security
    @GetMapping(path="/enrolments")
    public ResponseEntity<List<UserResponse>> getEnrolledStudents(@RequestParam(required = false) Long moduleId, @RequestParam(required = false) Long lecturerId) {
        return ResponseEntity.ok(enrolmentService.getEnrolledStudents(lecturerId, moduleId));
    }

    @PostMapping(path = "/enrolments")
    public ResponseEntity<EnrolmentResponse> enrol(@Valid @RequestBody CreateEnrolmentRequest request) {
        return new ResponseEntity<>(enrolmentService.enrol(request), HttpStatus.CREATED);
    }

    @GetMapping(path="/students/{studentId}/modules")
    public ResponseEntity<List<ModuleResponse>> getEnrolledModules(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrolmentService.getEnrolledModules(studentId));
    }

}
