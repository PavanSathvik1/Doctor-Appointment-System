package com.hms.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned on successful login.
 * Contains access token, refresh token, and user role information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** JWT access token (short-lived, 15 min). */
    private String accessToken;

    /** Refresh token UUID (long-lived, 7 days). Also set as httpOnly cookie. */
    private String refreshToken;

    /** Token type, always "Bearer". */
    @Builder.Default
    private String tokenType = "Bearer";

    /** The user's ID. */
    private Long userId;

    /** The user's email address. */
    private String email;

    /** The user's role (ADMIN, DOCTOR, PATIENT). */
    private String role;

    /** Access token expiry in milliseconds. */
    private Long expiresIn;
}
