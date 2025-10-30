package com.ten.devs.cards.cards.flashcards.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for approving AI-generated flashcard suggestions
 * Maps to POST /ai/sessions/{id}/approve endpoint
 * Based on flashcards table fields for content validation
 */
public record ApproveAiSuggestionsRequest(
    @NotEmpty(message = "At least one suggestion must be approved")
    @Valid
    List<ApprovedSuggestion> approvedSuggestions
) {

    /**
     * Individual suggestion approval with optional content modification
     * Based on flashcards table content fields (max 1000 chars each)
     */
    public record ApprovedSuggestion(
        @NotNull(message = "Suggestion ID is required")
        UUID suggestionId,

        @Size(max = 1000, message = "Front content cannot exceed 1000 characters")
        String frontContent,  // Optional - if edited, max 1000 chars

        @Size(max = 1000, message = "Back content cannot exceed 1000 characters")
        String backContent    // Optional - if edited, max 1000 chars
    ) {
    }
}