package com.hms.service.user;

import com.hms.entity.user.User;
import com.hms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * Loads user details from the database by email address.
 * The user's role is mapped to a Spring Security granted authority with the "ROLE_" prefix.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their email address (used as the "username" in Spring Security).
     *
     * @param email the user's email address
     * @return a fully populated {@link UserDetails} instance
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }

    /**
     * Loads user entity by email. Used by the JWT filter to obtain user context.
     *
     * @param email the user's email
     * @return the User entity
     * @throws UsernameNotFoundException if not found
     */
    public User loadUserEntityByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }
}
