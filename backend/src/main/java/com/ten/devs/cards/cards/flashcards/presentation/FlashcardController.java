package com.ten.devs.cards.cards.flashcards.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.flashcards.application.command.*;
import com.ten.devs.cards.cards.generated.api.FlashcardsApi;
import com.ten.devs.cards.cards.generated.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for flashcard management
 * Implements CRUD operations for user flashcards
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FlashcardController implements FlashcardsApi {

    private final Pipeline cqsService;

    @Override
    public ResponseEntity<GetFlashcardsResponse> getFlashcards(
            Integer page,
            Integer size,
            String sort,
            String source
    ) {
        log.info("Get flashcards request received: page={}, size={}, sort={}, source={}",
                page, size, sort, source);

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        GetFlashcardsCommand command = GetFlashcardsCommand.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .sort(sort)
                .source(source)
                .build();

        com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse domainResponse = cqsService.send(command);

        GetFlashcardsResponse response = new GetFlashcardsResponse();
        response.setContent(domainResponse.content().stream()
                .map(fc -> new FlashcardSummary(
                        fc.flashcardId(),
                        fc.frontContent(),
                        fc.backContent(),
                        FlashcardSummary.SourceEnum.fromValue(fc.source()),
                        fc.createdAt().atOffset(ZoneOffset.UTC),
                        fc.updatedAt().atOffset(ZoneOffset.UTC)
                ))
                .collect(Collectors.toList()));

        PageInfo pageInfo = new PageInfo(
                domainResponse.page().number(),
                domainResponse.page().size(),
                domainResponse.page().totalElements(),
                domainResponse.page().totalPages()
        );
        response.setPage(pageInfo);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CreateFlashcardResponse> createFlashcard(CreateFlashcardRequest createFlashcardRequest) {
        log.info("Create flashcard request received: frontContentLength={}, backContentLength={}",
                createFlashcardRequest.getFrontContent().length(), createFlashcardRequest.getBackContent().length());

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        CreateFlashcardCommand command = CreateFlashcardCommand.builder()
                .userId(userId)
                .frontContent(createFlashcardRequest.getFrontContent())
                .backContent(createFlashcardRequest.getBackContent())
                .build();

        com.ten.devs.cards.cards.flashcards.presentation.response.CreateFlashcardResponse domainResponse = cqsService.send(command);

        CreateFlashcardResponse response = new CreateFlashcardResponse(
                domainResponse.flashcardId(),
                domainResponse.frontContent(),
                domainResponse.backContent(),
                CreateFlashcardResponse.SourceEnum.fromValue(domainResponse.source()),
                domainResponse.createdAt().atOffset(ZoneOffset.UTC)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<UpdateFlashcardResponse> updateFlashcard(UUID flashcardId, UpdateFlashcardRequest updateFlashcardRequest) {
        log.info("Update flashcard request received: flashcardId={}, frontContentLength={}, backContentLength={}",
                flashcardId, updateFlashcardRequest.getFrontContent().length(), updateFlashcardRequest.getBackContent().length());

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        UpdateFlashcardCommand command = UpdateFlashcardCommand.builder()
                .userId(userId)
                .flashcardId(flashcardId)
                .frontContent(updateFlashcardRequest.getFrontContent())
                .backContent(updateFlashcardRequest.getBackContent())
                .build();

        com.ten.devs.cards.cards.flashcards.presentation.response.UpdateFlashcardResponse domainResponse = cqsService.send(command);

        UpdateFlashcardResponse response = new UpdateFlashcardResponse(
                domainResponse.flashcardId(),
                domainResponse.frontContent(),
                domainResponse.backContent(),
                UpdateFlashcardResponse.SourceEnum.fromValue(domainResponse.source()),
                domainResponse.updatedAt().atOffset(ZoneOffset.UTC)
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteFlashcard(UUID flashcardId) {
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