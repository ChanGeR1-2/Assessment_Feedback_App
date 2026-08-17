package com.dissertation.backend.common;

import com.dissertation.backend.app_users.exceptions.EmailExistsException;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.assessments.exceptions.*;
import com.dissertation.backend.auth.exceptions.InvalidPasswordException;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.common.exceptions.InvalidRoleException;
import com.dissertation.backend.course_modules.exceptions.ModuleExistsException;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import com.dissertation.backend.enrolment.exceptions.EnrolmentExistsException;
import com.dissertation.backend.feedback.exceptions.*;
import com.dissertation.backend.feedback_audio.exceptions.AudioExistsException;
import com.dissertation.backend.feedback_audio.exceptions.AudioNotFoundException;
import com.dissertation.backend.feedback_audio.exceptions.AudioStorageException;
import com.dissertation.backend.feedback_audio.exceptions.InvalidAudioException;
import com.dissertation.backend.phrases.exceptions.PhraseNotFoundException;
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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- Auth (401) ---

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return messageResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // --- Forbidden (403) ---

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException ex) {
        return messageResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // --- Not found (404) ---

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

    @ExceptionHandler(MarkingItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMarkingItemNotFound(MarkingItemNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AudioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAudioNotFound(AudioNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PhraseNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePhraseNotFound(PhraseNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MarkingItemNotForAssessmentException.class)
    public ResponseEntity<Map<String, String>> handleMarkingItemNotForAssessment(
            MarkingItemNotForAssessmentException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- Conflicts (409) ---

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

    @ExceptionHandler(RubricLockedException.class)
    public ResponseEntity<Map<String, String>> handleRubricLocked(RubricLockedException ex) {
        return messageResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AudioExistsException.class)
    public ResponseEntity<Map<String, String>> handleAudioExists(AudioExistsException ex) {
        return messageResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return messageResponse(HttpStatus.CONFLICT, "The operation conflicts with existing data");
    }



    // --- Bad requests (400) ---

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

    @ExceptionHandler(DuplicateMarkingItemException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateMarkingItem(DuplicateMarkingItemException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidMarkException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMark(InvalidMarkException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IncompleteFeedbackException.class)
    public ResponseEntity<Map<String, String>> handleIncompleteFeedback(IncompleteFeedbackException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidAudioException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAudio(InvalidAudioException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return messageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- Payload too large (413) ---

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> body = Map.of("message", "The audio file is too large");
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(body);
    }

    // --- Bean validation (@Valid) ---

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

    // --- Server errors (500) ---

    @ExceptionHandler(AudioStorageException.class)
    public ResponseEntity<Map<String, String>> handleAudioStorage(AudioStorageException ex) {
        log.error("Audio storage failure", ex);
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not save the audio recording");
    }

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