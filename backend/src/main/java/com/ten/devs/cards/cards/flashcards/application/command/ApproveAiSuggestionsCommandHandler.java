package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionRepository;
import com.ten.devs.cards.cards.flashcards.domain.Flashcard;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardRepository;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSnapshot;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSource;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion;
import com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest.ApprovedSuggestion;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse.CreatedFlashcard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handler for ApproveAiSuggestionsCommand
 * Approves and saves selected AI-generated flashcard suggestions.
 *
 * Business rules:
 * - Session must exist and be COMPLETED
 * - User must own the session
 * - All approved suggestion IDs must exist in the session
 * - Content comparison determines source: AI (unmodified) or AI_USER (edited)
 * - Creates Flashcard entities and persists them
 * - Updates session's acceptedCount
 *
 * Follows hexagonal architecture:
 * - Depends on domain repository PORTS (GENERIC interfaces)
 * - No dependencies on infrastructure implementations
 */
@Slf4j
@Component
@RequiredArgsConstructor
class ApproveAiSuggestionsCommandHandler implements Command.Handler<ApproveAiSuggestionsCommand, ApproveAiSuggestionsResponse> {

    private final AiGenerationSessionRepository sessionRepository;
    private final FlashcardRepository flashcardRepository;

    @Override
    public ApproveAiSuggestionsResponse handle(ApproveAiSuggestionsCommand command) {
        log.info("Approving {} suggestions for session: {}, user: {}",
            command.request().approvedSuggestions().size(),
            command.sessionId(),
            command.userId());

        // 1. Find session and verify it exists
        AiGenerationSession session = sessionRepository.findById(command.sessionId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Session not found: " + command.sessionId()));

        // 2. Verify ownership using domain method
        session.ensureOwnedBy(command.userId());

        // 3. Verify session can provide suggestions (must be COMPLETED)
        if (!session.canProvideSuggestions()) {
            throw new IllegalStateException(
                "Cannot approve suggestions for session in status: " + session.toSnapshot().status());
        }

        // 4. Get all suggestions from session
        List<FlashcardSuggestion> allSuggestions = session.getSuggestions();

        // Create map for quick lookup by ID
        Map<UUID, FlashcardSuggestion> suggestionMap = allSuggestions.stream()
            .collect(Collectors.toMap(FlashcardSuggestion::id, Function.identity()));

        // 5. Validate all approved suggestion IDs exist and create flashcard domain entities
        List<Flashcard> flashcardsToCreate = command.request().approvedSuggestions().stream()
            .map(approvedSuggestion -> {
                // Find original suggestion
                FlashcardSuggestion originalSuggestion = suggestionMap.get(approvedSuggestion.suggestionId());
                if (originalSuggestion == null) {
                    throw new IllegalArgumentException(
                        "Suggestion not found in session: " + approvedSuggestion.suggestionId());
                }

                // Determine final content (user-provided or original)
                String frontContent = approvedSuggestion.frontContent() != null
                    ? approvedSuggestion.frontContent()
                    : originalSuggestion.frontContent();

                String backContent = approvedSuggestion.backContent() != null
                    ? approvedSuggestion.backContent()
                    : originalSuggestion.backContent();

                // Determine source based on whether content was edited
                boolean wasEdited = approvedSuggestion.frontContent() != null
                    || approvedSuggestion.backContent() != null;
                FlashcardSource source = wasEdited ? FlashcardSource.AI_USER : FlashcardSource.AI;

                log.debug("Creating flashcard from suggestion {}: source={}, edited={}",
                    approvedSuggestion.suggestionId(), source, wasEdited);

                // Create Flashcard domain entity
                return Flashcard.createFromAiSuggestion(
                    command.userId(),
                    frontContent,
                    backContent,
                    source,
                    command.sessionId()
                );
            })
            .toList();

        // 6. Save all flashcards to database
        List<Flashcard> savedFlashcards = flashcardRepository.saveAll(flashcardsToCreate);

        log.info("Saved {} flashcards to database", savedFlashcards.size());

        // 7. Map to response DTOs using toSnapshot()
        List<CreatedFlashcard> createdFlashcards = savedFlashcards.stream()
            .map(flashcard -> {
                FlashcardSnapshot snapshot = flashcard.toSnapshot();
                return new CreatedFlashcard(
                    snapshot.id(),
                    snapshot.frontContent(),
                    snapshot.backContent(),
                    snapshot.source().name()
                );
            })
            .toList();

        // 8. Update session's acceptedCount using domain method
        session.updateAcceptedCount(createdFlashcards.size());
        sessionRepository.save(session);

        log.info("Approved {} flashcards for session {}, updated acceptedCount",
            createdFlashcards.size(), command.sessionId());

        return new ApproveAiSuggestionsResponse(createdFlashcards);
    }
}