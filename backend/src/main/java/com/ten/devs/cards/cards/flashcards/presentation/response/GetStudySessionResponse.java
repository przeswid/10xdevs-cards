package com.ten.devs.cards.cards.flashcards.presentation.response;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for study session flashcards
 * Maps to GET /study/session endpoint response
 * Based on flashcards table data for spaced repetition algorithm
 */
public record GetStudySessionResponse(
    List<StudyFlashcard> sessionFlashcards
) {
    
    /**
     * Flashcard data for study session
     * Based on flashcards table with minimal fields for studying
     */
    public record StudyFlashcard(
        UUID flashcardId,
        String frontContent,
        String backContent
    ) {
    }
}