package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateAiSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Handler for CreateAiGenerationSessionCommand
 * Creates a new AI flashcard generation session
 */
@Component
@RequiredArgsConstructor
class CreateAiGenerationSessionCommandHandler implements Command.Handler<CreateAiGenerationSessionCommand, CreateAiSessionResponse> {

    @Override
    public CreateAiSessionResponse handle(CreateAiGenerationSessionCommand command) {
        // TODO: Replace with actual logic:
        // - Validate input text length (1000-10000 chars)
        // - Create AI generation session in database with status=PENDING
        // - Trigger async AI generation process
        // For now, return dummy data

        UUID newSessionId = UUID.randomUUID();
        Instant now = Instant.now();

        return new CreateAiSessionResponse(
                newSessionId,
                "PENDING",
                now
        );
    }
}