package com.hms.repository.user;

import com.hms.entity.user.User;
import com.hms.entity.user.Role;
import com.hms.entity.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link User} entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     *
     * @param email the email address
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether a user with the given email already exists.
     *
     * @param email the email address
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     *
     * @param role the role to filter by
     * @return list of users with the given role
     */
    List<User> findByRole(Role role);

    /**
     * Find all users with a specific role and status.
     *
     * @param role   the role to filter by
     * @param status the status to filter by
     * @return list of matching users
     */
    List<User> findByRoleAndStatus(Role role, UserStatus status);

    /**
     * Count users by role.
     *
     * @param role the role to count
     * @return the number of users with the given role
     */
    long countByRole(Role role);
}
