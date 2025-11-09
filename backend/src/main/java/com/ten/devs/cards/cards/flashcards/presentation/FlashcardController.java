package com.ten.devs.cards.cards.flashcards.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.flashcards.application.command.*;
import com.ten.devs.cards.cards.flashcards.presentation.request.CreateFlashcardRequest;
import com.ten.devs.cards.cards.flashcards.presentation.request.UpdateFlashcardRequest;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateFlashcardResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.UpdateFlashcardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for flashcard management
 * Implements CRUD operations for user flashcards
 */
@Slf4j
@RequestMapping("/flashcards")
@RestController
@RequiredArgsConstructor
public class FlashcardController {

    private final Pipeline cqsService;

    /**
     * GET /flashcards - List user's flashcards with pagination
     */
    @GetMapping
    public ResponseEntity<GetFlashcardsResponse> getFlashcards(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String source
    ) {
        log.info("Get flashcards request received: page={}, size={}, sort={}, source={}",
                page, size, sort, source);

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("§"); // Dummy user ID

        GetFlashcardsCommand command = GetFlashcardsCommand.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .sort(sort)
                .source(source)
                .build();

        GetFlashcardsResponse response = cqsService.send(command);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /flashcards - Create a new manual flashcard
     */
    @PostMapping
    public ResponseEntity<CreateFlashcardResponse> createFlashcard(
            @Valid @RequestBody CreateFlashcardRequest request
    ) {
        log.info("Create flashcard request received: frontContentLength={}, backContentLength={}",
                request.frontContent().length(), request.backContent().length());

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        CreateFlashcardCommand command = CreateFlashcardCommand.builder()
                .userId(userId)
                .frontContent(request.frontContent())
                .backContent(request.backContent())
                .build();

        CreateFlashcardResponse response = cqsService.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /flashcards/{flashcardId} - Update existing flashcard
     */
    @PutMapping("/{flashcardId}")
    public ResponseEntity<UpdateFlashcardResponse> updateFlashcard(
            @PathVariable UUID flashcardId,
            @Valid @RequestBody UpdateFlashcardRequest request
    ) {
        log.info("Update flashcard request received: flashcardId={}, frontContentLength={}, backContentLength={}",
                flashcardId, request.frontContent().length(), request.backContent().length());

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        UpdateFlashcardCommand command = UpdateFlashcardCommand.builder()
                .userId(userId)
                .flashcardId(flashcardId)
                .frontContent(request.frontContent())
                .backContent(request.backContent())
                .build();

        UpdateFlashcardResponse response = cqsService.send(command);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /flashcards/{flashcardId} - Delete flashcard permanently
     */
    @DeleteMapping("/{flashcardId}")
    public ResponseEntity<Void> deleteFlashcard(@PathVariable UUID flashcardId) {
        log.info("Delete flashcard request received: flashcardId={}", flashcardId);

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        DeleteFlashcardCommand command = DeleteFlashcardCommand.builder()
                .userId(userId)
                .flashcardId(flashcardId)
                .build();

        cqsService.send(command);
        return ResponseEntity.noContent().build();
    }
}