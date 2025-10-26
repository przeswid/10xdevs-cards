package com.ten.devs.cards.cards.flashcards.presentation.request;

/**
 * Request DTO for manual flashcard creation
 * Maps to POST /flashcards endpoint
 * Based on flashcards table content fields
 */
public record CreateFlashcardRequest(
    String frontContent,  // 1-1000 characters, required
    String backContent    // 1-1000 characters, required
) {
}