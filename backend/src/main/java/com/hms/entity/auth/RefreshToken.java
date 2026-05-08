package com.hms.entity.auth;

import com.hms.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a long-lived refresh token.
 * <p>
 * Stored in the database (not Redis) and used to issue new access tokens
 * without requiring the user to re-authenticate. Has a 7-day expiry and
 * can be explicitly revoked on logout.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Checks whether this refresh token is expired.
     *
     * @return true if the token has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks whether this refresh token is valid (not expired and not revoked).
     *
     * @return true if the token can still be used
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
