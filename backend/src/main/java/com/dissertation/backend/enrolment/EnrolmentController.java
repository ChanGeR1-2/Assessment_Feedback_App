package com.dissertation.backend.enrolment;

import com.dissertation.backend.app_users.dto.UserResponse;
import com.dissertation.backend.enrolment.dto.CreateEnrolmentRequest;
import com.dissertation.backend.enrolment.dto.EnrolmentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrolments")
public class EnrolmentController {
    private final EnrolmentService enrolmentService;
    public EnrolmentController(EnrolmentService enrolmentService) {
        this.enrolmentService = enrolmentService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getEnrolledStudents(@RequestParam Long moduleId) {
        return new ResponseEntity<>(enrolmentService.getEnrolledStudents(moduleId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EnrolmentResponse> enrol(@Valid @RequestBody CreateEnrolmentRequest request) {
        return new ResponseEntity<>(enrolmentService.enrol(request), HttpStatus.CREATED);
    }

}
