package com.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 422 Unprocessable Entity.
 * <p>
 * Examples: trying to cancel an appointment less than 24h before,
 * creating a prescription for a non-completed appointment, etc.
 */
public class BusinessRuleException extends AppException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "business-rule-violation");
    }
}
