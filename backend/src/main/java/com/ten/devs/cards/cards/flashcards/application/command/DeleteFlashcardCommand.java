package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for deleting flashcard
 * Maps to DELETE /flashcards/{id} endpoint
 * Based on flashcards table entity with user ownership validation
 */
@Builder
public record DeleteFlashcardCommand(
    UUID userId,
    UUID flashcardId
) implements Command<Void> {
}