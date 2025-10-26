package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for AI-generated flashcard suggestions
 * Maps to GET /ai/sessions/{id}/suggestions endpoint response
 * Based on temporary suggestion data structure (not persisted in database)
 */
public record GetAiSuggestionsResponse(
    UUID sessionId,
    String status,          // Should be COMPLETED for suggestions to be available
    List<FlashcardSuggestion> suggestions
) {
    
    /**
     * Individual flashcard suggestion from AI
     * Based on flashcards table content structure
     */
    public record FlashcardSuggestion(
        UUID suggestionId,
        String frontContent,    // Max 1000 chars as per flashcards table
        String backContent      // Max 1000 chars as per flashcards table
    ) {
    }
}