package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetStudySessionResponse;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for retrieving flashcards for study session
 * Maps to GET /study/session endpoint
 * Based on flashcards table entity filtered for spaced repetition algorithm
 */
@Builder
public record GetStudySessionCommand(
    UUID userId
) implements Command<GetStudySessionResponse> {
}