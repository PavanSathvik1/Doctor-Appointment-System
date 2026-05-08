package com.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation would result in a conflict with existing state.
 * Maps to HTTP 409 Conflict.
 * <p>
 * Examples: duplicate email registration, double-booking an appointment slot.
 */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "conflict");
    }
}
