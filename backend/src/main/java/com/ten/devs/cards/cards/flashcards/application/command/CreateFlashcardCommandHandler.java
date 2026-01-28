package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.domain.Flashcard;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardRepository;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSnapshot;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateFlashcardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for CreateFlashcardCommand
 * Creates a new user-generated flashcard
 */
@Component
@RequiredArgsConstructor
class CreateFlashcardCommandHandler implements Command.Handler<CreateFlashcardCommand, CreateFlashcardResponse> {

    private final FlashcardRepository flashcardRepository;

    @Override
    public CreateFlashcardResponse handle(CreateFlashcardCommand command) {
        Flashcard flashcard = Flashcard.createManual(
                command.userId(),
                command.frontContent(),
                command.backContent()
        );

        Flashcard savedFlashcard = flashcardRepository.save(flashcard);
        FlashcardSnapshot snapshot = savedFlashcard.toSnapshot();

        return new CreateFlashcardResponse(
                snapshot.id(),
                snapshot.frontContent(),
                snapshot.backContent(),
                snapshot.source().name(),
                snapshot.createdAt()
        );
    }
}