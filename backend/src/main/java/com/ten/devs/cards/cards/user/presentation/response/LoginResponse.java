package com.ten.devs.cards.cards.user.presentation.response;

import java.util.UUID;

/**
 * Response DTO for user authentication
 * Maps to POST /auth/login endpoint response
 * Based on users table entity with JWT token data
 */
public record LoginResponse(
    String username,
    String accessToken,
    Long expiresIn
) {
}
