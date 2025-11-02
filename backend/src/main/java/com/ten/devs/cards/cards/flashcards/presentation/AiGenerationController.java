package com.ten.devs.cards.cards.flashcards.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.flashcards.application.command.*;
import com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest;
import com.ten.devs.cards.cards.flashcards.presentation.request.CreateAiSessionRequest;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateAiSessionResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSessionResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for AI flashcard generation
 * Implements AI generation workflow: create session, monitor status, retrieve suggestions, approve
 */
@Slf4j
@RequestMapping("/ai")
@RestController
@RequiredArgsConstructor
public class AiGenerationController {

    private final Pipeline cqsService;

    /**
     * POST /ai/sessions - Start AI flashcard generation session
     */
    @PostMapping("/sessions")
    public ResponseEntity<CreateAiSessionResponse> createAiSession(
            @Valid @RequestBody CreateAiSessionRequest request
    ) {
        log.info("Create AI session request received: inputTextLength={}", request.inputText().length());

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        CreateAiGenerationSessionCommand command = CreateAiGenerationSessionCommand.builder()
                .userId(userId)
                .inputText(request.inputText())
                .build();

        CreateAiSessionResponse response = cqsService.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /ai/sessions/{sessionId} - Get AI generation session status and metrics
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<GetAiSessionResponse> getAiSession(
            @PathVariable UUID sessionId
    ) {
        log.info("Get AI session request received: sessionId={}", sessionId);

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        GetAiGenerationSessionCommand command = GetAiGenerationSessionCommand.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();

        GetAiSessionResponse response = cqsService.send(command);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /ai/sessions/{sessionId}/suggestions - Retrieve AI-generated flashcard suggestions
     */
    @GetMapping("/sessions/{sessionId}/suggestions")
    public ResponseEntity<GetAiSuggestionsResponse> getAiSuggestions(
            @PathVariable UUID sessionId
    ) {
        log.info("Get AI suggestions request received: sessionId={}", sessionId);

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        GetAiSuggestionsCommand command = GetAiSuggestionsCommand.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();

        GetAiSuggestionsResponse response = cqsService.send(command);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /ai/sessions/{sessionId}/approve - Approve and save selected AI-generated flashcard suggestions
     */
    @PostMapping("/sessions/{sessionId}/approve")
    public ResponseEntity<ApproveAiSuggestionsResponse> approveAiSuggestions(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ApproveAiSuggestionsRequest request
    ) {
        log.info("Approve AI suggestions request received: sessionId={}, suggestionsCount={}",
                sessionId, request.approvedSuggestions().size());

        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        ApproveAiSuggestionsCommand command = ApproveAiSuggestionsCommand.builder()
                .userId(userId)
                .sessionId(sessionId)
                .request(request)
                .build();

        ApproveAiSuggestionsResponse response = cqsService.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}