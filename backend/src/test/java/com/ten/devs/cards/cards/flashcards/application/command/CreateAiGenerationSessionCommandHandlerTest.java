package com.ten.devs.cards.cards.flashcards.application.command;

import com.ten.devs.cards.cards.flashcards.application.service.AiServiceApi;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionRepository;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionStatus;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion;
import com.ten.devs.cards.cards.flashcards.presentation.response.CreateAiSessionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAiGenerationSessionCommandHandler")
class CreateAiGenerationSessionCommandHandlerTest {

    @Mock
    private AiServiceApi aiService;

    @Mock
    private AiGenerationSessionRepository sessionRepository;

    @InjectMocks
    private CreateAiGenerationSessionCommandHandler handler;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_INPUT_TEXT = "A".repeat(2000); // Valid input text (1000-10000 chars)
    private static final BigDecimal TEST_COST = new BigDecimal("0.05");

    private List<FlashcardSuggestion> createTestSuggestions(UUID sessionId, int count) {
        List<FlashcardSuggestion> suggestions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            suggestions.add(new FlashcardSuggestion(
                UUID.randomUUID(),
                sessionId,
                "Question " + i,
                "Answer " + i
            ));
        }
        return suggestions;
    }

    @Nested
    @DisplayName("Successful session creation")
    class SuccessfulSessionCreation {

        @Test
        @DisplayName("Given valid input, When AI service succeeds, Then should create COMPLETED session")
        void givenValidInput_whenAiServiceSucceeds_thenShouldCreateCompletedSession() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            // Mock AI service to return suggestions (sessionId will be generated in handler)
            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 5);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);

            // Capture saved session
            ArgumentCaptor<AiGenerationSession> sessionCaptor = ArgumentCaptor.forClass(AiGenerationSession.class);
            when(sessionRepository.save(sessionCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            CreateAiSessionResponse response = handler.handle(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.sessionId()).isNotNull();
            assertThat(response.status()).isEqualTo(AiGenerationSessionStatus.COMPLETED.name());
            assertThat(response.createdAt()).isNotNull();

            // Verify saved session
            AiGenerationSession savedSession = sessionCaptor.getValue();
            assertThat(savedSession.toSnapshot().status()).isEqualTo(AiGenerationSessionStatus.COMPLETED);
            assertThat(savedSession.toSnapshot().generatedCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Given valid input, When AI generates flashcards, Then should save session with suggestions")
        void givenValidInput_whenAiGeneratesFlashcards_thenShouldSaveSessionWithSuggestions() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 7);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);

            ArgumentCaptor<AiGenerationSession> sessionCaptor = ArgumentCaptor.forClass(AiGenerationSession.class);
            when(sessionRepository.save(sessionCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.handle(command);

            // Then
            AiGenerationSession savedSession = sessionCaptor.getValue();
            assertThat(savedSession.getSuggestions()).hasSize(7);
            assertThat(savedSession.toSnapshot().generatedCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("Given valid input, When creating session, Then should call AI service with pre-generated session ID")
        void givenValidInput_whenCreatingSession_thenShouldCallAiServiceWithPreGeneratedSessionId() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            ArgumentCaptor<UUID> sessionIdCaptor = ArgumentCaptor.forClass(UUID.class);
            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), sessionIdCaptor.capture()))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 3);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            CreateAiSessionResponse response = handler.handle(command);

            // Then
            UUID generatedSessionId = sessionIdCaptor.getValue();
            assertThat(generatedSessionId).isNotNull();
            assertThat(response.sessionId()).isEqualTo(generatedSessionId);
        }

        @Test
        @DisplayName("Given valid input, When AI service succeeds, Then should estimate cost")
        void givenValidInput_whenAiServiceSucceeds_thenShouldEstimateCost() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 5);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.handle(command);

            // Then
            verify(aiService, times(1)).estimateCost(TEST_INPUT_TEXT);
        }

        @Test
        @DisplayName("Given valid input, When creating session, Then should save session only once")
        void givenValidInput_whenCreatingSession_thenShouldSaveSessionOnlyOnce() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 5);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.handle(command);

            // Then
            verify(sessionRepository, times(1)).save(any(AiGenerationSession.class));
        }

        @Test
        @DisplayName("Given minimum valid input length, When AI service succeeds, Then should create session successfully")
        void givenMinimumValidInputLength_whenAiServiceSucceeds_thenShouldCreateSessionSuccessfully() {
            // Given
            String minInputText = "A".repeat(1000); // Minimum valid length
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                minInputText
            );

            when(aiService.generateFlashcards(eq(minInputText), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 3);
                });
            when(aiService.estimateCost(minInputText)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            CreateAiSessionResponse response = handler.handle(command);

            // Then
            assertThat(response.status()).isEqualTo(AiGenerationSessionStatus.COMPLETED.name());
        }

        @Test
        @DisplayName("Given maximum valid input length, When AI service succeeds, Then should create session successfully")
        void givenMaximumValidInputLength_whenAiServiceSucceeds_thenShouldCreateSessionSuccessfully() {
            // Given
            String maxInputText = "A".repeat(10000); // Maximum valid length
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                maxInputText
            );

            when(aiService.generateFlashcards(eq(maxInputText), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 10);
                });
            when(aiService.estimateCost(maxInputText)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            CreateAiSessionResponse response = handler.handle(command);

            // Then
            assertThat(response.status()).isEqualTo(AiGenerationSessionStatus.COMPLETED.name());
        }
    }

    @Nested
    @DisplayName("AI service failures")
    class AiServiceFailures {

        @Test
        @DisplayName("Given AI service throws exception, When handling command, Then should create FAILED session")
        void givenAiServiceThrowsException_whenHandlingCommand_thenShouldCreateFailedSession() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenThrow(new RuntimeException("AI service unavailable"));

            ArgumentCaptor<AiGenerationSession> sessionCaptor = ArgumentCaptor.forClass(AiGenerationSession.class);
            when(sessionRepository.save(sessionCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("AI service unavailable");

            // Verify FAILED session was saved
            AiGenerationSession savedSession = sessionCaptor.getValue();
            assertThat(savedSession.toSnapshot().status()).isEqualTo(AiGenerationSessionStatus.FAILED);
            assertThat(savedSession.toSnapshot().generatedCount()).isZero();
            assertThat(savedSession.getSuggestions()).isEmpty();
        }

        @Test
        @DisplayName("Given AI service fails, When handling command, Then should save session only once with FAILED status")
        void givenAiServiceFails_whenHandlingCommand_thenShouldSaveSessionOnlyOnceWithFailedStatus() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenThrow(new RuntimeException("AI service error"));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class);

            // Verify save called only once
            verify(sessionRepository, times(1)).save(any(AiGenerationSession.class));
        }

        @Test
        @DisplayName("Given AI service times out, When handling command, Then should create FAILED session and propagate exception")
        void givenAiServiceTimesOut_whenHandlingCommand_thenShouldCreateFailedSessionAndPropagateException() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenThrow(new RuntimeException("Request timeout"));

            ArgumentCaptor<AiGenerationSession> sessionCaptor = ArgumentCaptor.forClass(AiGenerationSession.class);
            when(sessionRepository.save(sessionCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Request timeout");

            // Verify FAILED session was saved
            AiGenerationSession savedSession = sessionCaptor.getValue();
            assertThat(savedSession.toSnapshot().status()).isEqualTo(AiGenerationSessionStatus.FAILED);
        }

        @Test
        @DisplayName("Given estimateCost throws exception, When handling command, Then should still create FAILED session")
        void givenEstimateCostThrowsException_whenHandlingCommand_thenShouldStillCreateFailedSession() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 5);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT))
                .thenThrow(new RuntimeException("Cost estimation failed"));

            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cost estimation failed");

            // Verify FAILED session was saved
            verify(sessionRepository, times(1)).save(
                argThat(session ->
                    session.toSnapshot().status() == AiGenerationSessionStatus.FAILED
                )
            );
        }
    }

    @Nested
    @DisplayName("Edge cases and validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Given AI returns empty suggestions list, When creating session, Then should throw IllegalArgumentException")
        void givenAiReturnsEmptySuggestionsList_whenCreatingSession_thenShouldThrowIllegalArgumentException() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenReturn(List.of());
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create completed session without suggestions");

            // Verify FAILED session was saved
            verify(sessionRepository, times(1)).save(
                argThat(session ->
                    session.toSnapshot().status() == AiGenerationSessionStatus.FAILED
                )
            );
        }

        @Test
        @DisplayName("Given AI returns null suggestions, When creating session, Then should throw IllegalArgumentException")
        void givenAiReturnsNullSuggestions_whenCreatingSession_thenShouldThrowIllegalArgumentException() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenReturn(null);
            // estimateCost not called when suggestions is null, so no stubbing needed
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AI service returned null suggestions");

            // Verify FAILED session was saved
            verify(sessionRepository, times(1)).save(
                argThat(session ->
                    session.toSnapshot().status() == AiGenerationSessionStatus.FAILED
                )
            );
        }

        @Test
        @DisplayName("Given repository throws exception when saving COMPLETED session, When handling command, Then should propagate exception")
        void givenRepositoryThrowsExceptionWhenSavingCompletedSession_whenHandlingCommand_thenShouldPropagateException() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 5);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenThrow(new RuntimeException("Database connection error"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");
        }

        @Test
        @DisplayName("Given repository throws exception when saving FAILED session, When handling command, Then should propagate exception")
        void givenRepositoryThrowsExceptionWhenSavingFailedSession_whenHandlingCommand_thenShouldPropagateException() {
            // Given
            CreateAiGenerationSessionCommand command = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenThrow(new RuntimeException("AI service error"));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenThrow(new RuntimeException("Database connection error"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");
        }

        @Test
        @DisplayName("Given multiple concurrent requests, When creating sessions, Then should generate unique session IDs")
        void givenMultipleConcurrentRequests_whenCreatingSessions_thenShouldGenerateUniqueSessionIds() {
            // Given
            CreateAiGenerationSessionCommand command1 = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            CreateAiGenerationSessionCommand command2 = new CreateAiGenerationSessionCommand(
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(aiService.generateFlashcards(eq(TEST_INPUT_TEXT), any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID sessionId = invocation.getArgument(1);
                    return createTestSuggestions(sessionId, 3);
                });
            when(aiService.estimateCost(TEST_INPUT_TEXT)).thenReturn(TEST_COST);
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            CreateAiSessionResponse response1 = handler.handle(command1);
            CreateAiSessionResponse response2 = handler.handle(command2);

            // Then
            assertThat(response1.sessionId()).isNotEqualTo(response2.sessionId());
        }
    }
}
