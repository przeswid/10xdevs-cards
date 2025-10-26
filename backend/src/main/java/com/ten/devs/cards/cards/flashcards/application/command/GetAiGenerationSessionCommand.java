package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSessionResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for retrieving AI generation session status
 * Maps to GET /ai/sessions/{id} endpoint
 * Based on ai_generation_sessions table entity
 */
@Builder
public record GetAiGenerationSessionCommand(
    UUID userId,
    UUID sessionId
) implements Command<GetAiSessionResponse> {
}