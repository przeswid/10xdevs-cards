package com.ten.devs.cards.cards.flashcards.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.config.auth.SecurityContextUserProvider;
import com.ten.devs.cards.cards.flashcards.application.command.*;
import com.ten.devs.cards.cards.generated.api.AiGenerationApi;
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
 * REST controller for AI flashcard generation
 * Implements AI generation workflow: create session, monitor status, retrieve suggestions, approve
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AiGenerationController implements AiGenerationApi {

    private final Pipeline cqsService;
    private final SecurityContextUserProvider securityContextUserProvider;

    @Override
    public ResponseEntity<CreateAiSessionResponse> createAiSession(CreateAiSessionRequest createAiSessionRequest) {
        log.info("Create AI session request received: inputTextLength={}", createAiSessionRequest.getInputText().length());

        UUID userId = securityContextUserProvider.getCurrentUserId();

        CreateAiGenerationSessionCommand command = CreateAiGenerationSessionCommand.builder()
                .userId(userId)
                .inputText(createAiSessionRequest.getInputText())
                .build();

        com.ten.devs.cards.cards.flashcards.presentation.response.CreateAiSessionResponse domainResponse = cqsService.send(command);

        CreateAiSessionResponse response = new CreateAiSessionResponse(
                domainResponse.sessionId(),
                CreateAiSessionResponse.StatusEnum.fromValue(domainResponse.status()),
                domainResponse.createdAt().atOffset(ZoneOffset.UTC)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<GetAiSessionResponse> getAiSession(UUID sessionId) {
        log.info("Get AI session request received: sessionId={}", sessionId);

        UUID userId = securityContextUserProvider.getCurrentUserId();

        GetAiGenerationSessionCommand command = GetAiGenerationSessionCommand.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();

        com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSessionResponse domainResponse = cqsService.send(command);

        GetAiSessionResponse response = new GetAiSessionResponse(
                domainResponse.sessionId(),
                GetAiSessionResponse.StatusEnum.fromValue(domainResponse.status()),
                domainResponse.generatedCount(),
                domainResponse.acceptedCount(),
                domainResponse.createdAt().atOffset(ZoneOffset.UTC)
        );

        // Set optional fields using setters
        if (domainResponse.aiModel() != null) {
            response.aiModel(domainResponse.aiModel());
        }
        if (domainResponse.apiCost() != null) {
            response.apiCost(domainResponse.apiCost().doubleValue());
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GetAiSuggestionsResponse> getAiSuggestions(UUID sessionId) {
        log.info("Get AI suggestions request received: sessionId={}", sessionId);

        UUID userId = securityContextUserProvider.getCurrentUserId();

        GetAiSuggestionsCommand command = GetAiSuggestionsCommand.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();

        com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse domainResponse = cqsService.send(command);

        GetAiSuggestionsResponse response = new GetAiSuggestionsResponse();
        response.setSessionId(domainResponse.sessionId());
        response.setStatus(GetAiSuggestionsResponse.StatusEnum.fromValue(domainResponse.status()));
        response.setSuggestions(domainResponse.suggestions().stream()
                .map(s -> new FlashcardSuggestion(
                        s.suggestionId(),
                        s.frontContent(),
                        s.backContent()
                ))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApproveAiSuggestionsResponse> approveAiSuggestions(
            UUID sessionId,
            ApproveAiSuggestionsRequest approveAiSuggestionsRequest
    ) {
        log.info("========== APPROVE ENDPOINT HIT ==========");
        log.info("Approve AI suggestions request received: sessionId={}, suggestionsCount={}",
                sessionId, approveAiSuggestionsRequest.getApprovedSuggestions().size());

        UUID userId = securityContextUserProvider.getCurrentUserId();

        // Convert generated request to domain request
        com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest domainRequest =
                new com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest(
                        approveAiSuggestionsRequest.getApprovedSuggestions().stream()
                                .map(s -> new com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest.ApprovedSuggestion(
                                        s.getSuggestionId(),
                                        s.getFrontContent().isPresent() ? s.getFrontContent().get() : null,
                                        s.getBackContent().isPresent() ? s.getBackContent().get() : null
                                ))
                                .toList()
                );

        ApproveAiSuggestionsCommand command = ApproveAiSuggestionsCommand.builder()
                .userId(userId)
                .sessionId(sessionId)
                .request(domainRequest)
                .build();

        com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse domainResponse = cqsService.send(command);

        ApproveAiSuggestionsResponse response = new ApproveAiSuggestionsResponse();
        response.setCreatedFlashcards(domainResponse.createdFlashcards().stream()
                .map(fc -> new CreatedFlashcard(
                        fc.flashcardId(),
                        fc.frontContent(),
                        fc.backContent(),
                        CreatedFlashcard.SourceEnum.fromValue(fc.source())
                ))
                .collect(Collectors.toList()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}