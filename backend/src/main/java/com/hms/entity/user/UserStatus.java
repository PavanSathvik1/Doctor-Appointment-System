package com.hms.entity.user;

/**
 * Enumeration of possible user account statuses.
 * Controls the user lifecycle from registration through activation and suspension.
 */
public enum UserStatus {
    /** Account created but OTP not yet verified. */
    PENDING,

    /** Account is active and fully operational. */
    ACTIVE,

    /** Account has been suspended by an administrator. */
    SUSPENDED
}
