package com.hms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base application exception for all HMS-specific runtime errors.
 * <p>
 * All custom exceptions extend this class. Each subclass defines
 * its own HTTP status code and error type slug for RFC 7807 responses.
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorSlug;

    public AppException(String message, HttpStatus httpStatus, String errorSlug) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorSlug = errorSlug;
    }

    public AppException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR, "GENERAL_ERROR");
    }

    public AppException(String message, Throwable cause, HttpStatus httpStatus, String errorSlug) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorSlug = errorSlug;
    }
}
