package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateFlashcardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Handler for CreateFlashcardCommand
 * Creates a new user-generated flashcard
 */
@Component
@RequiredArgsConstructor
class CreateFlashcardCommandHandler implements Command.Handler<CreateFlashcardCommand, CreateFlashcardResponse> {

    @Override
    public CreateFlashcardResponse handle(CreateFlashcardCommand command) {
        // TODO: Replace with actual domain logic and repository save
        // For now, return dummy data

        UUID newFlashcardId = UUID.randomUUID();
        Instant now = Instant.now();

        return new CreateFlashcardResponse(
                newFlashcardId,
                command.frontContent(),
                command.backContent(),
                "USER",  // Manually created flashcards have source = USER
                now
        );
    }
}