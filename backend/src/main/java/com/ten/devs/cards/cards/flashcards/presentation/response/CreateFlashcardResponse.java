package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for manual flashcard creation
 * Maps to POST /flashcards endpoint response
 * Based on flashcards table fields after creation
 */
public record CreateFlashcardResponse(
    UUID flashcardId,
    String frontContent,
    String backContent,
    String source,          // Always "USER" for manually created flashcards
    Instant createdAt
) {
}