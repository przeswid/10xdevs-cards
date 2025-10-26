package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for paginated flashcard listing
 * Maps to GET /flashcards endpoint response
 * Based on flashcards table with Spring Data pagination
 */
public record GetFlashcardsResponse(
    List<FlashcardSummary> content,
    PageInfo page
) {
    
    /**
     * Individual flashcard summary
     * Based on flashcards table fields
     */
    public record FlashcardSummary(
        UUID flashcardId,
        String frontContent,
        String backContent,
        String source,          // AI, AI_USER, USER
        Instant createdAt,
        Instant updatedAt
    ) {
    }
    
    /**
     * Pagination information
     * Based on Spring Data Pageable structure
     */
    public record PageInfo(
        Integer number,
        Integer size,
        Long totalElements,
        Integer totalPages
    ) {
    }
}