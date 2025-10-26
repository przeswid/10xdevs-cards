package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateFlashcardResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for creating manual flashcard
 * Maps to POST /flashcards endpoint
 * Based on flashcards table entity with source = USER
 */
@Builder
public record CreateFlashcardCommand(
    UUID userId,
    String frontContent,    // 1-1000 characters
    String backContent      // 1-1000 characters
) implements Command<CreateFlashcardResponse> {
}