package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for AI generation session status
 * Maps to GET /ai/sessions/{id} endpoint response
 * Based on ai_generation_sessions table fields
 */
public record GetAiSessionResponse(
    UUID sessionId,
    String status,              // PENDING, COMPLETED, FAILED
    Integer generatedCount,
    Integer acceptedCount,
    String aiModel,             // Optional
    BigDecimal apiCost,         // Optional, DECIMAL(10,4)
    Instant createdAt
) {
}