package com.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when input validation fails outside of Bean Validation.
 * Maps to HTTP 400 Bad Request.
 * <p>
 * Used for custom validation logic that cannot be expressed via Jakarta annotations.
 */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "validation-error");
    }
}
