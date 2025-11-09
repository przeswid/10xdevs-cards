package com.ten.devs.cards.cards.flashcards.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Snapshot (DTO) for transferring AiGenerationSession state.
 *
 * Used for:
 * - Reconstructing domain entity from database
 * - Transferring state to infrastructure layer
 * - Maintaining immutability of domain entity
 *
 * Contains all fields from the aggregate including suggestions.
 */
@Builder
public record AiGenerationSessionSnapshot(
    UUID id,
    UUID userId,
    String inputText,
    List<FlashcardSuggestion> suggestions,
    Integer generatedCount,
    Integer acceptedCount,
    String aiModel,
    BigDecimal apiCost,
    AiGenerationSessionStatus status,
    Instant createdAt
) {
    public AiGenerationSessionSnapshot {
        if (id == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at cannot be null");
        }
        // Default values for counts
        if (generatedCount == null) {
            generatedCount = 0;
        }
        if (acceptedCount == null) {
            acceptedCount = 0;
        }
    }
}