package com.ten.devs.cards.cards.flashcards.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Snapshot (DTO) for transferring Flashcard state.
 *
 * Used for:
 * - Reconstructing domain entity from database
 * - Transferring state to infrastructure layer
 * - Transferring state to presentation layer
 * - Maintaining immutability of domain entity
 *
 * Contains all fields from flashcards table.
 */
@Builder
public record FlashcardSnapshot(
    UUID id,
    UUID userId,
    String frontContent,
    String backContent,
    FlashcardSource source,
    UUID generationSessionId,
    Instant createdAt,
    Instant updatedAt
) {
}
