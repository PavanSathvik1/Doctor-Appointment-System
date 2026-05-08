package com.hms.dto.auth.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a refresh token in the request body.
 * Alternative to the httpOnly cookie approach.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    private String refreshToken;
}
