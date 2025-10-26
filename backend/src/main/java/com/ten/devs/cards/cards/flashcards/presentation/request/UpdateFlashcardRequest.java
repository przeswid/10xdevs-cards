package com.ten.devs.cards.cards.flashcards.presentation.request;

/**
 * Request DTO for updating existing flashcard
 * Maps to PUT /flashcards/{id} endpoint
 * Based on flashcards table content fields
 */
public record UpdateFlashcardRequest(
    String frontContent,  // 1-1000 characters, required
    String backContent    // 1-1000 characters, required
) {
}