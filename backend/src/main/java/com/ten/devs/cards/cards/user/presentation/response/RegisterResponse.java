package com.ten.devs.cards.cards.user.presentation.response;

import java.util.UUID;

/**
 * Response DTO for user registration
 * Maps to POST /auth/register endpoint response
 * Based on users table entity with JWT token data
 */
public record RegisterResponse(
    UUID userId
) {
}