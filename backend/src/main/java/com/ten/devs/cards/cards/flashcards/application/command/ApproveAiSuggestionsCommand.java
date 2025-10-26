package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for approving AI-generated flashcard suggestions
 * Maps to POST /ai/sessions/{id}/approve endpoint
 * Based on flashcards table entity creation from approved suggestions
 */
@Builder
public record ApproveAiSuggestionsCommand(
    UUID userId,
    UUID sessionId,
    ApproveAiSuggestionsRequest request
) implements Command<ApproveAiSuggestionsResponse> {
}