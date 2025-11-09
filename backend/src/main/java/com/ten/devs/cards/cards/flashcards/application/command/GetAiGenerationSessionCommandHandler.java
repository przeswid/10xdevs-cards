package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionRepository;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionSnapshot;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for GetAiGenerationSessionCommand
 * Retrieves AI generation session status and metrics.
 *
 * Business rules:
 * - Session must exist
 * - User must own the session
 *
 * Follows hexagonal architecture:
 * - Depends on domain repository PORTS (GENERIC interfaces)
 * - No dependencies on infrastructure implementations
 */
@Slf4j
@Component
@RequiredArgsConstructor
class GetAiGenerationSessionCommandHandler implements Command.Handler<GetAiGenerationSessionCommand, GetAiSessionResponse> {

    private final AiGenerationSessionRepository sessionRepository;

    @Override
    public GetAiSessionResponse handle(GetAiGenerationSessionCommand command) {
        log.info("Retrieving AI generation session: {}, user: {}",
            command.sessionId(), command.userId());

        // 1. Find session and verify it exists
        AiGenerationSession session = sessionRepository.findById(command.sessionId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Session not found: " + command.sessionId()));

        // 2. Verify ownership using domain method
        session.ensureOwnedBy(command.userId());

        // 3. Get snapshot to access state
        AiGenerationSessionSnapshot snapshot = session.toSnapshot();

        log.debug("Found session {} with status: {}", snapshot.id(), snapshot.status());

        // 4. Map to response DTO
        return new GetAiSessionResponse(
            snapshot.id(),
            snapshot.status().name(),
            snapshot.generatedCount(),
            snapshot.acceptedCount(),
            snapshot.aiModel(),
            snapshot.apiCost(),
            snapshot.createdAt()
        );
    }
}