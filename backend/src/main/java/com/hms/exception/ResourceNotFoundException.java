package com.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "resource-not-found");
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " with ID " + id + " not found", HttpStatus.NOT_FOUND, "resource-not-found");
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(resourceName + " with " + field + " '" + value + "' not found",
                HttpStatus.NOT_FOUND, "resource-not-found");
    }
}
