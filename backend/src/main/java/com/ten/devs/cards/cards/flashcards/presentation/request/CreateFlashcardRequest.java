package com.ten.devs.cards.cards.flashcards.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for manual flashcard creation
 * Maps to POST /flashcards endpoint
 * Based on flashcards table content fields
 */
public record CreateFlashcardRequest(
    @NotBlank(message = "Front content is required")
    @Size(min = 1, max = 1000, message = "Front content must be between 1 and 1000 characters")
    String frontContent,

    @NotBlank(message = "Back content is required")
    @Size(min = 1, max = 1000, message = "Back content must be between 1 and 1000 characters")
    String backContent
) {
}