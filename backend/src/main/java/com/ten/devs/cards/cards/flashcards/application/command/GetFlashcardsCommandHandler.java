package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse.FlashcardSummary;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handler for GetFlashcardsCommand
 * Retrieves user's flashcards with pagination and filtering
 */
@Component
@RequiredArgsConstructor
class GetFlashcardsCommandHandler implements Command.Handler<GetFlashcardsCommand, GetFlashcardsResponse> {

    @Override
    public GetFlashcardsResponse handle(GetFlashcardsCommand command) {
        // TODO: Replace with actual repository query with pagination
        // For now, return dummy data

        Instant now = Instant.now();

        List<FlashcardSummary> dummyFlashcards = List.of(
                new FlashcardSummary(
                        UUID.randomUUID(),
                        "What is the capital of France?",
                        "Paris",
                        "USER",
                        now.minusSeconds(3600),
                        now.minusSeconds(3600)
                ),
                new FlashcardSummary(
                        UUID.randomUUID(),
                        "What is 2 + 2?",
                        "4",
                        "AI",
                        now.minusSeconds(7200),
                        now.minusSeconds(7200)
                ),
                new FlashcardSummary(
                        UUID.randomUUID(),
                        "What is the largest planet?",
                        "Jupiter",
                        "AI_USER",
                        now.minusSeconds(10800),
                        now.minusSeconds(1800)
                )
        );

        PageInfo pageInfo = new PageInfo(
                command.page() != null ? command.page() : 0,
                command.size() != null ? command.size() : 20,
                3L,  // Total elements
                1    // Total pages
        );

        return new GetFlashcardsResponse(dummyFlashcards, pageInfo);
    }
}