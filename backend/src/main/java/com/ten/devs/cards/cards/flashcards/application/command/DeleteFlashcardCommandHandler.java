package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for DeleteFlashcardCommand
 * Deletes a flashcard permanently after verifying ownership
 */
@Component
@RequiredArgsConstructor
class DeleteFlashcardCommandHandler implements Command.Handler<DeleteFlashcardCommand, Void> {

    @Override
    public Void handle(DeleteFlashcardCommand command) {
        // TODO: Replace with actual domain logic
        // - Find flashcard by ID
        // - Verify ownership (userId matches)
        // - Delete flashcard
        // - Throw 404 if not found or 403 if not owned by user
        // For now, just simulate successful deletion

        System.out.println("Simulating deletion of flashcard: " + command.flashcardId() + " for user: " + command.userId());

        return null;
    }
}