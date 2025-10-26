package com.ten.devs.cards.cards.flashcards.presentation.request;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for approving AI-generated flashcard suggestions
 * Maps to POST /ai/sessions/{id}/approve endpoint
 * Based on flashcards table fields for content validation
 */
public record ApproveAiSuggestionsRequest(
    List<ApprovedSuggestion> approvedSuggestions
) {
    
    /**
     * Individual suggestion approval with optional content modification
     * Based on flashcards table content fields (max 1000 chars each)
     */
    public record ApprovedSuggestion(
        UUID suggestionId,
        String frontContent,  // Optional - if edited, max 1000 chars
        String backContent    // Optional - if edited, max 1000 chars
    ) {
    }
}