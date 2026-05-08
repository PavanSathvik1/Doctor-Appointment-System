package com.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails or credentials are invalid.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorisedException extends AppException {

    public UnauthorisedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "unauthorised");
    }
}
