package com.hms.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single field-level validation error.
 * <p>
 * Included in the {@link ErrorResponse#getErrors()} list when
 * Bean Validation (@Valid) fails on a request body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {

    /** The name of the field that failed validation. */
    private String field;

    /** The validation error message. */
    private String message;
}
