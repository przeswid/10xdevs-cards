package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for approved AI suggestions
 * Maps to POST /ai/sessions/{id}/approve endpoint response
 * Based on flashcards table fields after creation
 */
public record ApproveAiSuggestionsResponse(
    List<CreatedFlashcard> createdFlashcards
) {
    
    /**
     * Individual created flashcard from approved suggestion
     * Based on flashcards table structure
     */
    public record CreatedFlashcard(
        UUID flashcardId,
        String frontContent,
        String backContent,
        String source           // AI or AI_USER (depending on whether content was edited)
    ) {
    }
}