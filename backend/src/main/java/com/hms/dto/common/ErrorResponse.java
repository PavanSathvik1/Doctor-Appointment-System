package com.hms.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * RFC 7807 Problem Details error response DTO.
 * <p>
 * Every error response from the API adheres to this structure,
 * providing a consistent and machine-readable error format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** A URI reference identifying the error type (e.g., "https://hms.app/errors/resource-not-found"). */
    private String type;

    /** A short, human-readable summary of the problem. */
    private String title;

    /** The HTTP status code. */
    private int status;

    /** A human-readable explanation specific to this occurrence of the problem. */
    private String detail;

    /** ISO 8601 timestamp of when the error occurred. */
    private Instant timestamp;

    /** Field-level validation errors (only present for validation failures). */
    private List<FieldError> errors;
}
