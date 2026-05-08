package com.hms.repository.auth;

import com.hms.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link RefreshToken} entity operations.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its token string.
     *
     * @param token the UUID token string
     * @return an Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Revoke all active (non-revoked) refresh tokens for a specific user.
     * Used during logout to invalidate all sessions.
     *
     * @param userId the user's ID
     * @return the number of tokens revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Delete all expired refresh tokens. Used by cleanup scheduler.
     *
     * @return the number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP")
    int deleteAllExpired();
}
