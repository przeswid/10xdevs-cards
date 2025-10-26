package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for flashcard update
 * Maps to PUT /flashcards/{id} endpoint response
 * Based on flashcards table fields after update
 */
public record UpdateFlashcardResponse(
    UUID flashcardId,
    String frontContent,
    String backContent,
    String source,          // AI_USER (if AI-generated then modified) or USER (if manually created)
    Instant updatedAt
) {
}