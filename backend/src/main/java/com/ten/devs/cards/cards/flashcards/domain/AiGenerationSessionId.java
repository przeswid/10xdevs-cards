package com.ten.devs.cards.cards.flashcards.domain;

import java.util.UUID;

/**
 * Value object representing AI generation session identifier.
 *
 * Provides strong typing for session IDs and encapsulates validation logic.
 * Immutable by design.
 */
public record AiGenerationSessionId(UUID value) {

    public AiGenerationSessionId {
        if (value == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
    }

    /**
     * Creates a new random session ID.
     */
    public static AiGenerationSessionId generate() {
        return new AiGenerationSessionId(UUID.randomUUID());
    }

    /**
     * Creates session ID from existing UUID.
     */
    public static AiGenerationSessionId of(UUID uuid) {
        return new AiGenerationSessionId(uuid);
    }

    /**
     * Creates session ID from string representation.
     */
    public static AiGenerationSessionId of(String uuidString) {
        try {
            return new AiGenerationSessionId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid session ID format: " + uuidString, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}