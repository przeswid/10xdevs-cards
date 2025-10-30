package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse.FlashcardSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for GetAiSuggestionsCommand
 * Retrieves AI-generated flashcard suggestions for a session
 */
@Component
@RequiredArgsConstructor
class GetAiSuggestionsCommandHandler implements Command.Handler<GetAiSuggestionsCommand, GetAiSuggestionsResponse> {

    @Override
    public GetAiSuggestionsResponse handle(GetAiSuggestionsCommand command) {
        // TODO: Replace with actual logic:
        // - Find session by ID
        // - Verify ownership (userId matches)
        // - Check session status is COMPLETED
        // - Retrieve AI-generated suggestions
        // - Throw 404 if not found, 403 if not owned, 409 if not completed
        // For now, return dummy data

        List<FlashcardSuggestion> dummySuggestions = List.of(
                new FlashcardSuggestion(
                        UUID.randomUUID(),
                        "What is polymorphism in OOP?",
                        "Polymorphism allows objects of different types to be treated as objects of a common super type"
                ),
                new FlashcardSuggestion(
                        UUID.randomUUID(),
                        "What is encapsulation?",
                        "Encapsulation is the bundling of data and methods that operate on that data within a single unit"
                ),
                new FlashcardSuggestion(
                        UUID.randomUUID(),
                        "What is inheritance?",
                        "Inheritance is a mechanism where a new class derives properties and behaviors from an existing class"
                ),
                new FlashcardSuggestion(
                        UUID.randomUUID(),
                        "What is abstraction?",
                        "Abstraction is the process of hiding implementation details and showing only functionality to the user"
                )
        );

        return new GetAiSuggestionsResponse(
                command.sessionId(),
                "COMPLETED",
                dummySuggestions
        );
    }
}