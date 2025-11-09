package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionRepository;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionSnapshot;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse.FlashcardSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for GetAiSuggestionsCommand
 * Retrieves AI-generated flashcard suggestions from the AiGenerationSession aggregate.
 *
 * Business rules:
 * - Session must exist
 * - User must own the session
 * - Session must be COMPLETED (suggestions only available after generation)
 *
 * Follows hexagonal architecture:
 * - Depends on domain repository PORTS (GENERIC interfaces)
 * - No dependencies on infrastructure implementations
 *
 * Suggestions are now part of the AiGenerationSession aggregate.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class GetAiSuggestionsCommandHandler implements Command.Handler<GetAiSuggestionsCommand, GetAiSuggestionsResponse> {

    private final AiGenerationSessionRepository sessionRepository;

    @Override
    public GetAiSuggestionsResponse handle(GetAiSuggestionsCommand command) {
        log.info("Retrieving AI suggestions for session: {}, user: {}",
            command.sessionId(), command.userId());

        // 1. Find session and verify it exists
        AiGenerationSession session = sessionRepository.findById(command.sessionId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Session not found: " + command.sessionId()));

        // 2. Verify ownership using domain method
        session.ensureOwnedBy(command.userId());

        // 3. Check if session can provide suggestions using domain method
        if (!session.canProvideSuggestions()) {
            // Get snapshot to extract status for response
            AiGenerationSessionSnapshot snapshot = session.toSnapshot();
            log.info("Session {} cannot provide suggestions, status: {}",
                command.sessionId(), snapshot.status());

            return new GetAiSuggestionsResponse(
                command.sessionId(),
                snapshot.status().name(),
                List.of()
            );
        }

        // 4. Retrieve suggestions from the aggregate using business method
        List<com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion> domainSuggestions =
            session.getSuggestions();

        log.info("Found {} suggestions for session {}",
            domainSuggestions.size(), command.sessionId());

        // 5. Map domain suggestions to response DTOs
        List<FlashcardSuggestion> suggestions = domainSuggestions.stream()
            .map(domainSuggestion -> new FlashcardSuggestion(
                domainSuggestion.id(),
                domainSuggestion.frontContent(),
                domainSuggestion.backContent()
            ))
            .toList();

        // 6. Get snapshot for response data
        AiGenerationSessionSnapshot snapshot = session.toSnapshot();

        return new GetAiSuggestionsResponse(
            command.sessionId(),
            snapshot.status().name(),
            suggestions
        );
    }
}