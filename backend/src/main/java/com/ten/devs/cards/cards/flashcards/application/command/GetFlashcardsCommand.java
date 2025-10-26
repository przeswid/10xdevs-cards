package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for retrieving user's flashcards with pagination
 * Maps to GET /flashcards endpoint
 * Based on flashcards table entity with pagination support
 */
@Builder
public record GetFlashcardsCommand(
    UUID userId,
    Integer page,           // Default 0
    Integer size,           // Default 20, max 100
    String sort,            // Default "createdAt,desc"
    String source           // Optional filter: AI, AI_USER, USER
) implements Command<GetFlashcardsResponse> {
}