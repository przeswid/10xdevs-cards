package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.application.service.AiServiceApi;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionRepository;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionSnapshot;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateAiSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Handler for CreateAiGenerationSessionCommand
 * Creates a new AI flashcard generation session and generates flashcards using AI service.
 *
 * Follows hexagonal architecture:
 * - Depends on domain repository PORTS (GENERIC interfaces)
 * - Depends on AI service PORT (GENERIC interface)
 * - No dependencies on infrastructure implementations (ADAPTERS)
 *
 * Suggestions are now part of the AiGenerationSession aggregate.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class CreateAiGenerationSessionCommandHandler implements Command.Handler<CreateAiGenerationSessionCommand, CreateAiSessionResponse> {

    // Dependencies on GENERIC INTERFACES (Ports), not concrete implementations (Adapters)
    private final AiServiceApi aiService;
    private final AiGenerationSessionRepository sessionRepository;

    @Override
    public CreateAiSessionResponse handle(CreateAiGenerationSessionCommand command) {
        log.info("Creating AI generation session for user: {}, input length: {}",
            command.userId(), command.inputText().length());

        // 1. Generate session ID upfront (before creating domain object)
        UUID sessionId = UUID.randomUUID();
        log.debug("Generated session ID: {}", sessionId);

        AiGenerationSession session;

        try {
            // 2. Generate flashcards using AI service with pre-generated session ID
            List<FlashcardSuggestion> suggestions = aiService.generateFlashcards(
                command.inputText(),
                sessionId
            );

            // 3. Validate suggestions
            if (suggestions == null) {
                throw new IllegalArgumentException("AI service returned null suggestions");
            }

            log.info("Generated {} flashcard suggestions for session {}",
                suggestions.size(), sessionId);

            // 4. Estimate cost (in production, get actual cost from API response)
            BigDecimal estimatedCost = aiService.estimateCost(command.inputText());

            // 5. Create domain object with COMPLETED status and suggestions
            // This will throw IllegalArgumentException if suggestions is empty
            session = AiGenerationSession.createCompleted(
                sessionId,
                command.userId(),
                command.inputText(),
                suggestions,
                "openai/gpt-4o-mini",  // TODO: Get actual model from API response
                estimatedCost
            );

            // 6. Save session ONCE with COMPLETED status and suggestions
            // Suggestions are saved automatically as part of the aggregate
            session = sessionRepository.save(session);

            AiGenerationSessionSnapshot snapshot = session.toSnapshot();
            log.info("Session {} saved as COMPLETED with {} suggestions",
                snapshot.id(), suggestions.size());

            return new CreateAiSessionResponse(
                snapshot.id(),
                snapshot.status().name(),
                snapshot.createdAt()
            );

        } catch (Exception e) {
            log.error("Failed to generate flashcards for session {}", sessionId, e);

            // Create domain object with FAILED status
            session = AiGenerationSession.createFailed(
                sessionId,
                command.userId(),
                command.inputText()
            );

            // Save session ONCE with FAILED status
            session = sessionRepository.save(session);

            AiGenerationSessionSnapshot snapshot = session.toSnapshot();
            log.warn("Session {} saved as FAILED", snapshot.id());

            // Re-throw exception to be handled by global exception handler
            throw e;
        }
    }
}