package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for AI generation session creation
 * Maps to POST /ai/sessions endpoint response
 * Based on ai_generation_sessions table fields
 */
public record CreateAiSessionResponse(
    UUID sessionId,
    String status,      // PENDING, COMPLETED, FAILED
    Instant createdAt
) {
}