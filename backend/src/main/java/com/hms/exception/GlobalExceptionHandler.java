package com.hms.exception;

import com.hms.dto.common.ErrorResponse;
import com.hms.dto.common.FieldError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Global exception handler for the HMS application.
 * <p>
 * Catches all application-specific and framework exceptions and converts them
 * into standardised RFC 7807 Problem Details JSON responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_URI = "https://hms.app/errors/";

    /**
     * Handles all custom {@link AppException} subclasses.
     * Maps each to its declared HTTP status code and error slug.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        log.warn("Application exception: {} - {}", ex.getErrorSlug(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .type(ERROR_BASE_URI + ex.getErrorSlug())
                .title(formatTitle(ex.getErrorSlug()))
                .status(ex.getHttpStatus().value())
                .detail(ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * Handles Bean Validation failures from {@code @Valid} annotations.
     * Extracts field-level errors and returns a structured 400 response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .type(ERROR_BASE_URI + "validation-error")
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more fields failed validation")
                .timestamp(Instant.now())
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles Spring Security access denied exceptions (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .type(ERROR_BASE_URI + "access-denied")
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You do not have permission to access this resource")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Catches all uncaught exceptions as a fallback.
     * Returns a generic 500 Internal Server Error while logging the full stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse response = ErrorResponse.builder()
                .type(ERROR_BASE_URI + "internal-server-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Converts a slug like "resource-not-found" to "Resource Not Found".
     */
    private String formatTitle(String slug) {
        String[] parts = slug.split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
