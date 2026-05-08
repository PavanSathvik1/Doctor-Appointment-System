package com.example.demo.service.auth;

import com.hms.dto.auth.request.*;
import com.hms.dto.auth.response.AuthResponse;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.auth.response.TokenRefreshResponse;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new patient user and sends OTP for verification.
     *
     * @param request the registration details
     * @return a message indicating OTP has been sent
     */
    MessageResponse register(RegisterRequest request);

    /**
     * Verifies the OTP code sent during registration and activates the user account.
     *
     * @param request the OTP verification details (email + OTP code)
     * @return a message indicating account activation status
     */
    MessageResponse verifyOtp(OtpVerifyRequest request);

    /**
     * Authenticates a user by email and password, issuing JWT access and refresh tokens.
     *
     * @param request the login credentials
     * @return the authentication response with tokens and user info
     */
    AuthResponse login(LoginRequest request);

    /**
     * Issues a new access token using a valid refresh token.
     *
     * @param refreshTokenStr the refresh token UUID string
     * @return the new access token response
     */
    TokenRefreshResponse refreshToken(String refreshTokenStr);

    /**
     * Logs out the user by revoking all their tokens.
     *
     * @param userId the user's ID
     * @return a message confirming logout
     */
    MessageResponse logout(Long userId);

    /**
     * Initiates the password reset flow by sending a reset link via email.
     *
     * @param request the forgot password request containing the user's email
     * @return a message indicating the email has been sent
     */
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Resets the user's password using a valid reset token.
     *
     * @param request the reset request containing the token and new password
     * @return a message confirming the password has been reset
     */
    MessageResponse resetPassword(ResetPasswordRequest request);
}
