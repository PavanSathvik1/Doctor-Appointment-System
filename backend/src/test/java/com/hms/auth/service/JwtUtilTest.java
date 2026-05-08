package com.hms.auth.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private JwtUtil jwtUtil;

    // Use a fixed valid base64 key for testing (at least 256 bits)
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "this_is_a_test_secret_key_that_must_be_at_least_256_bits_long".getBytes());
    private static final long TEST_EXPIRY_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_EXPIRY_MS, redisTemplate);
    }

    @Test
    void generateAccessToken_ShouldReturnValidTokenAndStoreInRedis() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        Long userId = 1L;
        String email = "test@example.com";
        String role = "PATIENT";

        // Act
        String token = jwtUtil.generateAccessToken(userId, email, role);

        // Assert
        assertNotNull(token);
        verify(valueOperations, times(1)).set(
                eq("auth:access:" + userId),
                eq(token),
                eq(TEST_EXPIRY_MS),
                eq(TimeUnit.MILLISECONDS)
        );
        
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(role, claims.get("role", String.class));
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValidAndMatchesRedis() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        Long userId = 2L;
        String token = jwtUtil.generateAccessToken(userId, "doc@hms.com", "DOCTOR");
        
        when(valueOperations.get("auth:access:" + userId)).thenReturn(token);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenNotInRedis() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        Long userId = 3L;
        String token = jwtUtil.generateAccessToken(userId, "admin@hms.com", "ADMIN");
        
        when(valueOperations.get("auth:access:" + userId)).thenReturn(null); // Simulated logout

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void revokeAccessToken_ShouldDeleteFromRedis() {
        // Arrange
        Long userId = 4L;

        // Act
        jwtUtil.revokeAccessToken(userId);

        // Assert
        verify(redisTemplate, times(1)).delete("auth:access:" + userId);
    }
}
