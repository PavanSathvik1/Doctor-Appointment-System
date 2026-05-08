package com.hms.controller.auth;

import com.hms.dto.auth.request.*;
import com.hms.dto.auth.response.AuthResponse;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.auth.response.TokenRefreshResponse;
import com.hms.service.auth.AuthService;
import com.hms.entity.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * REST controller for authentication operations.
 * <p>
 * Handles patient registration, OTP verification, login, token refresh,
 * logout, and password reset flows.
 * <p>
 * All endpoints are prefixed with {@code /api/auth}.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new patient account and sends an OTP for email verification.
     *
     * @param request the registration details
     * @return 201 Created with a confirmation message
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verifies the OTP code sent during registration.
     * On success, the user account is activated.
     *
     * @param request the OTP verification details
     * @return 200 OK with activation confirmation
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        MessageResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates a user and returns JWT access and refresh tokens.
     * The refresh token is also set as an httpOnly cookie.
     *
     * @param request the login credentials
     * @return 200 OK with tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        // Set refresh token as httpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    /**
     * Issues a new access token using a valid refresh token.
     * Accepts the refresh token from either the request body or httpOnly cookie.
     *
     * @param refreshTokenBody refresh token from the request body (optional)
     * @param refreshTokenCookie refresh token from the cookie (optional)
     * @return 200 OK with the new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenBody,
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        String refreshToken = null;

        // Prefer cookie over body
        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            refreshToken = refreshTokenCookie;
        } else if (refreshTokenBody != null && refreshTokenBody.getRefreshToken() != null) {
            refreshToken = refreshTokenBody.getRefreshToken();
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TokenRefreshResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out the authenticated user by revoking all tokens.
     *
     * @param user the currently authenticated user
     * @return 200 OK with logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal User user) {
        MessageResponse response = authService.logout(user.getId());

        // Clear refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(response);
    }

    /**
     * Initiates the password reset flow by sending a reset link via email.
     *
     * @param request the request containing the user's email
     * @return 200 OK with confirmation message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Resets the user's password using a valid reset token.
     *
     * @param request the request containing the reset token and new password
     * @return 200 OK with confirmation message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
