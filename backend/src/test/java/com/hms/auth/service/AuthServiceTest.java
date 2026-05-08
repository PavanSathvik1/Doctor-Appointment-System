package com.hms.auth.service;

import com.hms.auth.dto.request.LoginRequest;
import com.hms.auth.dto.request.OtpVerifyRequest;
import com.hms.auth.dto.request.RegisterRequest;
import com.hms.auth.dto.response.AuthResponse;
import com.hms.auth.dto.response.MessageResponse;
import com.hms.auth.entity.RefreshToken;
import com.hms.auth.repository.RefreshTokenRepository;
import com.hms.common.exception.BusinessRuleException;
import com.hms.common.exception.ConflictException;
import com.hms.common.exception.UnauthorisedException;
import com.hms.common.exception.ValidationException;
import com.hms.notification.service.NotificationService;
import com.hms.user.entity.Role;
import com.hms.user.entity.User;
import com.hms.user.entity.UserStatus;
import com.hms.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldCreateUserAndSendOtp_WhenEmailIsUnique() {
        // Arrange
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password", "1234567890");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed_password");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        MessageResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(valueOperations, times(1)).set(eq("auth:otp:john@example.com"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(notificationService, times(1)).sendOtpEmail(eq("john@example.com"), eq("John"), anyString());
    }

    @Test
    void register_ShouldThrowConflictException_WhenEmailExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password", "1234567890");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyOtp_ShouldActivateUser_WhenOtpIsValid() {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest("john@example.com", "123456");
        User pendingUser = User.builder().email("john@example.com").status(UserStatus.PENDING).build();
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:otp:john@example.com")).thenReturn("123456");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(pendingUser));

        // Act
        MessageResponse response = authService.verifyOtp(request);

        // Assert
        assertNotNull(response);
        assertEquals(UserStatus.ACTIVE, pendingUser.getStatus());
        verify(userRepository, times(1)).save(pendingUser);
        verify(redisTemplate, times(1)).delete("auth:otp:john@example.com");
        verify(notificationService, times(1)).sendWelcomeEmail("john@example.com", "User");
    }

    @Test
    void verifyOtp_ShouldThrowValidationException_WhenOtpIsInvalid() {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest("john@example.com", "wrong");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:otp:john@example.com")).thenReturn("123456");

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.verifyOtp(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsAreValidAndUserIsActive() {
        // Arrange
        LoginRequest request = new LoginRequest("active@example.com", "password");
        User activeUser = User.builder()
                .id(1L)
                .email("active@example.com")
                .passwordHash("hashed")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .build();
                
        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtUtil.generateAccessToken(1L, "active@example.com", "PATIENT")).thenReturn("access_token");
        when(refreshTokenRepository.revokeAllByUserId(1L)).thenReturn(0);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("active@example.com", response.getEmail());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void login_ShouldThrowUnauthorisedException_WhenPasswordIsIncorrect() {
        // Arrange
        LoginRequest request = new LoginRequest("user@example.com", "wrong_password");
        User user = User.builder().email("user@example.com").passwordHash("hashed").build();
                
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "hashed")).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorisedException.class, () -> authService.login(request));
    }

    @Test
    void login_ShouldThrowBusinessRuleException_WhenUserIsPending() {
        // Arrange
        LoginRequest request = new LoginRequest("pending@example.com", "password");
        User pendingUser = User.builder().email("pending@example.com").passwordHash("hashed").status(UserStatus.PENDING).build();
                
        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> authService.login(request));
    }
}
