package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse.CreatedFlashcard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for ApproveAiSuggestionsCommand
 * Approves and saves selected AI-generated flashcard suggestions
 */
@Component
@RequiredArgsConstructor
class ApproveAiSuggestionsCommandHandler implements Command.Handler<ApproveAiSuggestionsCommand, ApproveAiSuggestionsResponse> {

    @Override
    public ApproveAiSuggestionsResponse handle(ApproveAiSuggestionsCommand command) {
        // TODO: Replace with actual logic:
        // - Find session by ID and verify ownership
        // - Validate all suggestion IDs exist in the session
        // - For each approved suggestion:
        //   * Check if content was edited (compare with original)
        //   * Set source to AI if unmodified, AI_USER if edited
        //   * Create flashcard with generation_session_id
        // - Update session's accepted_count
        // - Return created flashcards
        // For now, return dummy data

        List<CreatedFlashcard> dummyCreatedFlashcards = command.request().approvedSuggestions().stream()
                .map(suggestion -> {
                    // Simulate determining if content was edited
                    boolean wasEdited = suggestion.frontContent() != null || suggestion.backContent() != null;
                    String source = wasEdited ? "AI_USER" : "AI";

                    return new CreatedFlashcard(
                            UUID.randomUUID(),
                            suggestion.frontContent() != null ? suggestion.frontContent() : "What is polymorphism?",
                            suggestion.backContent() != null ? suggestion.backContent() : "Polymorphism allows...",
                            source
                    );
                })
                .toList();

        return new ApproveAiSuggestionsResponse(dummyCreatedFlashcards);
    }
}