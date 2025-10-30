package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Handler for GetAiGenerationSessionCommand
 * Retrieves AI generation session status and metrics
 */
@Component
@RequiredArgsConstructor
class GetAiGenerationSessionCommandHandler implements Command.Handler<GetAiGenerationSessionCommand, GetAiSessionResponse> {

    @Override
    public GetAiSessionResponse handle(GetAiGenerationSessionCommand command) {
        // TODO: Replace with actual logic:
        // - Find session by ID
        // - Verify ownership (userId matches)
        // - Return session status and metrics
        // - Throw 404 if not found, 403 if not owned by user
        // For now, return dummy data

        return new GetAiSessionResponse(
                command.sessionId(),
                "COMPLETED",
                12,  // generatedCount
                8,   // acceptedCount
                "gpt-4",
                new BigDecimal("0.0250"),  // apiCost
                Instant.now().minusSeconds(600)  // created 10 minutes ago
        );
    }
}