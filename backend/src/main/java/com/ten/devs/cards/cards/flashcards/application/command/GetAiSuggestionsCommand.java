package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for retrieving AI-generated flashcard suggestions
 * Maps to GET /ai/sessions/{id}/suggestions endpoint
 * Based on ai_generation_sessions table with temporary suggestion data
 */
@Builder
public record GetAiSuggestionsCommand(
    UUID userId,
    UUID sessionId
) implements Command<GetAiSuggestionsResponse> {
}