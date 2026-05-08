package com.hms.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned when a new access token is issued via refresh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {

    /** New JWT access token. */
    private String accessToken;

    /** Token type, always "Bearer". */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Access token expiry in milliseconds. */
    private Long expiresIn;
}
