package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.UpdateFlashcardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handler for UpdateFlashcardCommand
 * Updates existing flashcard content and adjusts source tracking
 */
@Component
@RequiredArgsConstructor
class UpdateFlashcardCommandHandler implements Command.Handler<UpdateFlashcardCommand, UpdateFlashcardResponse> {

    @Override
    public UpdateFlashcardResponse handle(UpdateFlashcardCommand command) {
        // TODO: Replace with actual domain logic
        // - Find flashcard by ID
        // - Verify ownership (userId matches)
        // - Update content
        // - If source was AI, change to AI_USER
        // - Save and return
        // For now, return dummy data

        Instant now = Instant.now();

        return new UpdateFlashcardResponse(
                command.flashcardId(),
                command.frontContent(),
                command.backContent(),
                "AI_USER",  // Simulating an edited AI-generated card
                now
        );
    }
}