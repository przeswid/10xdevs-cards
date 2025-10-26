package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.UpdateFlashcardResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for updating existing flashcard
 * Maps to PUT /flashcards/{id} endpoint
 * Based on flashcards table entity with source tracking for modifications
 */
@Builder
public record UpdateFlashcardCommand(
    UUID userId,
    UUID flashcardId,
    String frontContent,    // 1-1000 characters
    String backContent      // 1-1000 characters
) implements Command<UpdateFlashcardResponse> {
}