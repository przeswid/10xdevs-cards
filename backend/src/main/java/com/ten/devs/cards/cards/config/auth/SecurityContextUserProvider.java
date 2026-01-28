package com.ten.devs.cards.cards.config.auth;

import com.ten.devs.cards.cards.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Provides the current authenticated user's ID from Spring Security context.
 * Extracts username from JWT authentication and retrieves user ID from repository.
 */
@Component
@RequiredArgsConstructor
public class SecurityContextUserProvider {

    private final UserRepository userRepository;

    /**
     * Retrieves the current authenticated user's ID.
     *
     * @return UUID of the authenticated user
     * @throws IllegalStateException if no user is authenticated or user not found
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }

        String username = authentication.getName();

        return userRepository.getUserByUsername(username)
                .map(user -> user.toSnapshot().id().id())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
    }
}
