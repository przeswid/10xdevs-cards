package com.ten.devs.cards.cards.flashcards.domain;

/**
 * Status of AI flashcard generation session.
 *
 * Domain-driven enum representing the lifecycle of a generation session.
 */
public enum AiGenerationSessionStatus {
    /**
     * Session created, generation in progress.
     */
    PENDING,

    /**
     * Generation completed successfully, flashcards generated.
     */
    COMPLETED,

    /**
     * Generation failed due to error (API failure, validation error, etc.).
     */
    FAILED
}