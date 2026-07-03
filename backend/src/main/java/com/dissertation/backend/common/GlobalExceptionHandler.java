package com.dissertation.backend.common;

import com.dissertation.backend.app_users.exceptions.EmailExistsException;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.assessments.exceptions.AssessmentNotFoundException;
import com.dissertation.backend.assessments.exceptions.InvalidModuleException;
import com.dissertation.backend.auth.exceptions.InvalidPasswordException;
import com.dissertation.backend.common.exceptions.InvalidRoleException;
import com.dissertation.backend.course_modules.exceptions.ModuleExistsException;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import com.dissertation.backend.enrolment.exceptions.EnrolmentExistsException;
import com.dissertation.backend.feedback.exceptions.FeedbackExistsException;
import com.dissertation.backend.feedback.exceptions.FeedbackNotFoundException;
import com.dissertation.backend.feedback.exceptions.StudentNotEnrolledException;
import com.dissertation.backend.feedback.exceptions.UnauthorisedLecturerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- Auth ---

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return messageResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // --- Forbidden ---

    @ExceptionHandler(UnauthorisedLecturerException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorisedLecturer(UnauthorisedLecturerException ex) {
        return messageResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // --- Not found ---

    @ExceptionHandler(ModuleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleModuleNotFound(ModuleNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AssessmentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAssessmentNotFound(AssessmentNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(FeedbackNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFeedbackNotFound(FeedbackNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- Conflicts ---

    @ExceptionHandler(EmailExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(EmailExistsException ex) {
        return messageResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ModuleExistsException.class)
    public ResponseEntity<Map<String, String>> handleModuleExists(ModuleExistsException ex) {
        return messageResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(EnrolmentExistsException.class)
    public ResponseEntity<Map<String, String>> handleEnrolmentExists(EnrolmentExistsException ex) {
        return messageResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FeedbackExistsException.class)
    public ResponseEntity<Map<String, String>> handleFeedbackExists(FeedbackExistsException ex) {
        return messageResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return messageResponse(HttpStatus.CONFLICT, "The operation conflicts with existing data");
    }

    // --- Bad requests ---

    @ExceptionHandler(InvalidModuleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidModule(InvalidModuleException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRole(InvalidRoleException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(StudentNotEnrolledException.class)
    public ResponseEntity<Map<String, String>> handleStudentNotEnrolled(StudentNotEnrolledException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- Bean validation (@Valid) — overrides the parent's handler ---

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // --- Catch-all: log the real cause, return a generic message ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception", ex);
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // --- Helper ---

    private ResponseEntity<Map<String, String>> messageResponse(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}