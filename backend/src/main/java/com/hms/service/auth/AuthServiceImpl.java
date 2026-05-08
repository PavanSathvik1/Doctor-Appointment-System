package com.hms.service.auth;

import com.hms.dto.auth.request.*;
import com.hms.dto.auth.response.AuthResponse;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.auth.response.TokenRefreshResponse;
import com.hms.entity.auth.RefreshToken;
import com.hms.repository.auth.RefreshTokenRepository;
import com.hms.exception.*;
import com.hms.service.notification.NotificationService;
import com.hms.entity.user.Role;
import com.hms.entity.user.User;
import com.hms.entity.user.UserStatus;
import com.hms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link AuthService} providing complete authentication workflows.
 * <p>
 * Handles patient registration with OTP verification, JWT-based login with Redis-backed
 * token storage, refresh token rotation, and password reset via email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REDIS_OTP_PREFIX = "auth:otp:";
    private static final String REDIS_RESET_PREFIX = "auth:reset:";
    private static final long OTP_TTL_MINUTES = 5;
    private static final long RESET_TTL_MINUTES = 15;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;

    @Value("${app.jwt.expiry-ms}")
    private long jwtExpiryMs;

    @Value("${app.refresh-token.expiry-days}")
    private int refreshTokenExpiryDays;

    /**
     * {@inheritDoc}
     * <p>
     * Creates a PATIENT user with PENDING status, generates a 6-digit OTP,
     * stores it in Redis with 5-minute TTL, and sends it via email.
     */
    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with email '" + request.getEmail() + "' already exists");
        }

        // Create user with PENDING status
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.PATIENT)
                .status(UserStatus.PENDING)
                .build();
        user = userRepository.save(user);

        // Generate and store OTP
        String otp = generateOtp();
        String redisKey = REDIS_OTP_PREFIX + request.getEmail();
        redisTemplate.opsForValue().set(redisKey, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);

        // Send OTP email
        notificationService.sendOtpEmail(request.getEmail(), request.getFirstName(), otp);

        log.info("Patient registered: email={}. OTP sent.", request.getEmail());
        return MessageResponse.builder()
                .message("Registration successful. Please check your email for the OTP verification code.")
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates the OTP against Redis, activates the user account, and sends a welcome email.
     */
    @Override
    @Transactional
    public MessageResponse verifyOtp(OtpVerifyRequest request) {
        String redisKey = REDIS_OTP_PREFIX + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new BusinessRuleException("OTP has expired or was not generated. Please request a new one.");
        }

        if (!storedOtp.equals(request.getOtp())) {
            throw new ValidationException("Invalid OTP. Please check and try again.");
        }

        // Activate user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessRuleException("Account is already verified and active.");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Delete OTP from Redis
        redisTemplate.delete(redisKey);

        // Send welcome email
        notificationService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("OTP verified and account activated: email={}", request.getEmail());
        return MessageResponse.builder()
                .message("Email verified successfully. Your account is now active. You can log in.")
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates credentials, checks account status, generates JWT access token
     * (stored in Redis) and refresh token (stored in DB), and returns both.
     */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorisedException("Invalid email or password"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorisedException("Invalid email or password");
        }

        // Check account status
        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessRuleException("Account not verified. Please verify your email with the OTP sent during registration.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessRuleException("Your account has been suspended. Please contact support.");
        }

        // Generate access token (stored in Redis by JwtUtil)
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        // Generate refresh token (stored in DB)
        String refreshTokenStr = createRefreshToken(user);

        log.info("User logged in: email={}, role={}", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtExpiryMs)
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates the refresh token from DB (not expired, not revoked),
     * then issues a new access token.
     */
    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new UnauthorisedException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new UnauthorisedException("Refresh token has expired or been revoked. Please log in again.");
        }

        User user = refreshToken.getUser();

        // Check user is still active
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("Your account is not active. Please contact support.");
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        log.info("Access token refreshed for userId={}", user.getId());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtExpiryMs)
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Revokes all refresh tokens in DB and removes the access token from Redis.
     */
    @Override
    @Transactional
    public MessageResponse logout(Long userId) {
        // Revoke access token from Redis
        jwtUtil.revokeAccessToken(userId);

        // Revoke all refresh tokens in DB
        refreshTokenRepository.revokeAllByUserId(userId);

        log.info("User logged out: userId={}", userId);
        return MessageResponse.builder()
                .message("Successfully logged out.")
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Generates a UUID reset token, stores it in Redis with 15-minute TTL,
     * and sends a reset link via email.
     */
    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        // Always return success to prevent email enumeration
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return MessageResponse.builder()
                    .message("If an account with that email exists, a password reset link has been sent.")
                    .build();
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        String redisKey = REDIS_RESET_PREFIX + resetToken;
        redisTemplate.opsForValue().set(redisKey, user.getEmail(), RESET_TTL_MINUTES, TimeUnit.MINUTES);

        // Send reset email
        notificationService.sendPasswordResetEmail(user.getEmail(), resetToken);

        log.info("Password reset email sent: email={}", request.getEmail());
        return MessageResponse.builder()
                .message("If an account with that email exists, a password reset link has been sent.")
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates the reset token from Redis, updates the user's password,
     * and deletes the token.
     */
    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String redisKey = REDIS_RESET_PREFIX + request.getToken();
        String email = redisTemplate.opsForValue().get(redisKey);

        if (email == null) {
            throw new BusinessRuleException("Reset token is invalid or has expired. Please request a new one.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete reset token from Redis
        redisTemplate.delete(redisKey);

        // Revoke all existing tokens for security
        jwtUtil.revokeAccessToken(user.getId());
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password reset successful for email={}", email);
        return MessageResponse.builder()
                .message("Password has been reset successfully. Please log in with your new password.")
                .build();
    }

    // ─── Private Helpers ───────────────────────────────────────────

    /**
     * Generates a 6-digit OTP using a secure random number generator.
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Creates a new refresh token for the given user and stores it in the database.
     */
    private String createRefreshToken(User user) {
        // Revoke any existing refresh tokens for this user
        refreshTokenRepository.revokeAllByUserId(user.getId());

        String tokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenStr;
    }
}
