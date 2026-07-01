package com.dissertation.backend.common;

import com.dissertation.backend.app_users.exceptions.EmailExistsException;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.assessments.exceptions.InvalidModuleException;
import com.dissertation.backend.auth.exceptions.InvalidPasswordException;
import com.dissertation.backend.course_modules.exceptions.ModuleExistsException;
import com.dissertation.backend.course_modules.exceptions.ModuleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // --- Not found ---

    @ExceptionHandler(ModuleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleModuleNotFound(ModuleNotFoundException ex) {
        return messageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
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

    // --- Bad requests ---

    @ExceptionHandler(InvalidModuleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidModule(InvalidModuleException ex) {
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