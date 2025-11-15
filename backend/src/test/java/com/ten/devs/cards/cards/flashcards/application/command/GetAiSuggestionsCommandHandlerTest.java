package com.ten.devs.cards.cards.flashcards.application.command;

import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionRepository;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionStatus;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetAiSuggestionsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAiSuggestionsCommandHandler")
class GetAiSuggestionsCommandHandlerTest {

    @Mock
    private AiGenerationSessionRepository sessionRepository;

    @InjectMocks
    private GetAiSuggestionsCommandHandler handler;

    private static final UUID TEST_SESSION_ID = UUID.fromString("88f87c49-7e7e-440a-a4e3-a3fb919e06f1");
    private static final UUID TEST_USER_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final UUID OTHER_USER_ID = UUID.fromString("87654321-4321-4321-4321-210987654321");
    private static final String TEST_INPUT_TEXT = "A".repeat(2000);

    private List<FlashcardSuggestion> createTestSuggestions(int count) {
        List<FlashcardSuggestion> suggestions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            suggestions.add(new FlashcardSuggestion(
                UUID.randomUUID(),
                TEST_SESSION_ID,
                "Question " + i,
                "Answer " + i
            ));
        }
        return suggestions;
    }

    @Nested
    @DisplayName("Successful retrieval")
    class SuccessfulRetrieval {

        @Test
        @DisplayName("Given COMPLETED session with suggestions, When retrieving suggestions, Then should return all suggestions")
        void givenCompletedSessionWithSuggestions_whenRetrievingSuggestions_thenShouldReturnAllSuggestions() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.05")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.sessionId()).isEqualTo(TEST_SESSION_ID);
            assertThat(response.status()).isEqualTo(AiGenerationSessionStatus.COMPLETED.name());
            assertThat(response.suggestions()).hasSize(5);
            assertThat(response.suggestions().get(0).frontContent()).isEqualTo("Question 0");
            assertThat(response.suggestions().get(0).backContent()).isEqualTo("Answer 0");
        }

        @Test
        @DisplayName("Given COMPLETED session, When retrieving suggestions, Then should verify ownership")
        void givenCompletedSession_whenRetrievingSuggestions_thenShouldVerifyOwnership() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.05")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            handler.handle(command);

            // Then - no exception thrown means ownership was verified successfully
            verify(sessionRepository, times(1)).findById(TEST_SESSION_ID);
        }

        @Test
        @DisplayName("Given COMPLETED session with many suggestions, When retrieving, Then should return all suggestions in order")
        void givenCompletedSessionWithManySuggestions_whenRetrieving_thenShouldReturnAllSuggestionsInOrder() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            List<FlashcardSuggestion> suggestions = createTestSuggestions(10);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.10")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.suggestions()).hasSize(10);
            for (int i = 0; i < 10; i++) {
                assertThat(response.suggestions().get(i).frontContent()).isEqualTo("Question " + i);
                assertThat(response.suggestions().get(i).backContent()).isEqualTo("Answer " + i);
            }
        }

        @Test
        @DisplayName("Given COMPLETED session, When retrieving suggestions, Then response DTOs should contain suggestion IDs")
        void givenCompletedSession_whenRetrievingSuggestions_thenResponseDtosShouldContainSuggestionIds() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.05")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.suggestions()).allMatch(dto -> dto.suggestionId() != null);
        }
    }

    @Nested
    @DisplayName("Session status checks")
    class SessionStatusChecks {

        @Test
        @DisplayName("Given PENDING session, When retrieving suggestions, Then should return empty suggestions list")
        void givenPendingSession_whenRetrievingSuggestions_thenShouldReturnEmptySuggestionsList() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, TEST_INPUT_TEXT);
            UUID actualSessionId = session.toSnapshot().id();

            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, actualSessionId);

            when(sessionRepository.findById(actualSessionId)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.sessionId()).isEqualTo(actualSessionId);
            assertThat(response.status()).isEqualTo(AiGenerationSessionStatus.PENDING.name());
            assertThat(response.suggestions()).isEmpty();
        }

        @Test
        @DisplayName("Given FAILED session, When retrieving suggestions, Then should return empty suggestions list")
        void givenFailedSession_whenRetrievingSuggestions_thenShouldReturnEmptySuggestionsList() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            AiGenerationSession session = AiGenerationSession.createFailed(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.sessionId()).isEqualTo(TEST_SESSION_ID);
            assertThat(response.status()).isEqualTo(AiGenerationSessionStatus.FAILED.name());
            assertThat(response.suggestions()).isEmpty();
        }

        @Test
        @DisplayName("Given session transitions from PENDING to COMPLETED, When retrieving, Then should reflect current status")
        void givenSessionTransitionsFromPendingToCompleted_whenRetrieving_thenShouldReflectCurrentStatus() {
            // Given
            // Create session in PENDING state
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, TEST_INPUT_TEXT);
            UUID actualSessionId = session.toSnapshot().id();

            // Complete the session
            List<FlashcardSuggestion> suggestions = List.of(
                new FlashcardSuggestion(UUID.randomUUID(), actualSessionId, "Q1", "A1"),
                new FlashcardSuggestion(UUID.randomUUID(), actualSessionId, "Q2", "A2"),
                new FlashcardSuggestion(UUID.randomUUID(), actualSessionId, "Q3", "A3")
            );
            session.complete(suggestions, "openai/gpt-4o-mini", new BigDecimal("0.05"));

            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, actualSessionId);
            when(sessionRepository.findById(actualSessionId)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.status()).isEqualTo(AiGenerationSessionStatus.COMPLETED.name());
            assertThat(response.suggestions()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Error cases")
    class ErrorCases {

        @Test
        @DisplayName("Given non-existent session ID, When retrieving suggestions, Then should throw IllegalArgumentException")
        void givenNonExistentSessionId_whenRetrievingSuggestions_thenShouldThrowIllegalArgumentException() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Session not found: " + TEST_SESSION_ID);
        }

        @Test
        @DisplayName("Given session owned by different user, When retrieving suggestions, Then should throw IllegalArgumentException")
        void givenSessionOwnedByDifferentUser_whenRetrievingSuggestions_thenShouldThrowIllegalArgumentException() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(OTHER_USER_ID, TEST_SESSION_ID);

            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID, // Different user
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.05")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not owned by user");
        }

        @Test
        @DisplayName("Given repository throws exception, When retrieving suggestions, Then should propagate exception")
        void givenRepositoryThrowsException_whenRetrievingSuggestions_thenShouldPropagateException() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            when(sessionRepository.findById(TEST_SESSION_ID))
                .thenThrow(new RuntimeException("Database connection error"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Given COMPLETED session with single suggestion, When retrieving, Then should return one suggestion")
        void givenCompletedSessionWithSingleSuggestion_whenRetrieving_thenShouldReturnOneSuggestion() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            List<FlashcardSuggestion> suggestions = createTestSuggestions(1);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.01")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.suggestions()).hasSize(1);
            assertThat(response.suggestions().get(0).frontContent()).isEqualTo("Question 0");
        }

        @Test
        @DisplayName("Given suggestions with special characters, When retrieving, Then should preserve content exactly")
        void givenSuggestionsWithSpecialCharacters_whenRetrieving_thenShouldPreserveContentExactly() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            String specialFront = "What is \"polymorphism\"?";
            String specialBack = "It's a concept where objects can take <multiple> forms & types.";

            List<FlashcardSuggestion> suggestions = List.of(
                new FlashcardSuggestion(
                    UUID.randomUUID(),
                    TEST_SESSION_ID,
                    specialFront,
                    specialBack
                )
            );

            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.02")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.suggestions().get(0).frontContent()).isEqualTo(specialFront);
            assertThat(response.suggestions().get(0).backContent()).isEqualTo(specialBack);
        }

        @Test
        @DisplayName("Given same session queried multiple times, When retrieving suggestions, Then should return consistent results")
        void givenSameSessionQueriedMultipleTimes_whenRetrievingSuggestions_thenShouldReturnConsistentResults() {
            // Given
            GetAiSuggestionsCommand command = new GetAiSuggestionsCommand(TEST_USER_ID, TEST_SESSION_ID);

            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT,
                suggestions,
                "openai/gpt-4o-mini",
                new BigDecimal("0.05")
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When
            GetAiSuggestionsResponse response1 = handler.handle(command);
            GetAiSuggestionsResponse response2 = handler.handle(command);

            // Then
            assertThat(response1.suggestions()).hasSize(3);
            assertThat(response2.suggestions()).hasSize(3);
            assertThat(response1.suggestions().get(0).frontContent())
                .isEqualTo(response2.suggestions().get(0).frontContent());
        }
    }
}
