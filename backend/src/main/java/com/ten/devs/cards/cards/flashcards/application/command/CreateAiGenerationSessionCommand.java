package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateAiSessionResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for creating AI generation session
 * Maps to POST /ai/sessions endpoint
 * Based on ai_generation_sessions table entity
 */
@Builder
public record CreateAiGenerationSessionCommand(
    UUID userId,
    String inputText        // 1000-10000 characters as per database constraint
) implements Command<CreateAiSessionResponse> {
}