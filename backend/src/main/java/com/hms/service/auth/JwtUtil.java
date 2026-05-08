package com.hms.service.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for JWT token generation, parsing, and validation.
 * <p>
 * Access tokens are signed with HS256 and stored in Redis for instant revocation.
 * Every request validates the token against Redis — not just the signature and expiry.
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String REDIS_ACCESS_PREFIX = "auth:access:";

    private final SecretKey signingKey;
    private final long expiryMs;
    private final StringRedisTemplate redisTemplate;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiry-ms}") long expiryMs,
            StringRedisTemplate redisTemplate) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiryMs = expiryMs;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generates a signed JWT access token containing user claims.
     * The token is also stored in Redis for validation on each request.
     *
     * @param userId the user's database ID
     * @param email  the user's email address
     * @param role   the user's role (ADMIN, DOCTOR, PATIENT)
     * @return the signed JWT string
     */
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        String token = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        // Store in Redis with TTL matching token expiry
        String redisKey = REDIS_ACCESS_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, token, expiryMs, TimeUnit.MILLISECONDS);

        log.debug("Generated access token for userId={}, expires at {}", userId, expiry);
        return token;
    }

    /**
     * Parses the JWT and extracts all claims.
     *
     * @param token the JWT string
     * @return the parsed {@link Claims}
     * @throws JwtException if the token is invalid or expired
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the email (subject) from the JWT.
     *
     * @param token the JWT string
     * @return the email address
     */
    public String getEmailFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extracts the userId claim from the JWT.
     *
     * @param token the JWT string
     * @return the user ID
     */
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * Extracts the role claim from the JWT.
     *
     * @param token the JWT string
     * @return the role string
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * Validates the JWT by checking:
     * 1. Signature is valid (HS256)
     * 2. Token is not expired
     * 3. Token matches the one stored in Redis (instant revocation support)
     *
     * @param token the JWT string
     * @return true if the token is fully valid
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            Long userId = claims.get("userId", Long.class);

            // Check Redis for token match (revocation check)
            String redisKey = REDIS_ACCESS_PREFIX + userId;
            String storedToken = redisTemplate.opsForValue().get(redisKey);

            if (storedToken == null || !storedToken.equals(token)) {
                log.warn("Token validation failed: Redis mismatch for userId={}", userId);
                return false;
            }

            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation error: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Revokes the access token for a user by deleting it from Redis.
     * Called on logout.
     *
     * @param userId the user ID whose token to revoke
     */
    public void revokeAccessToken(Long userId) {
        String redisKey = REDIS_ACCESS_PREFIX + userId;
        redisTemplate.delete(redisKey);
        log.info("Revoked access token for userId={}", userId);
    }
}
