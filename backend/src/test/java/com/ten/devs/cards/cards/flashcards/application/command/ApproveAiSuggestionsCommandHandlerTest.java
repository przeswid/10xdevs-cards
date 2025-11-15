package com.ten.devs.cards.cards.flashcards.application.command;

import com.ten.devs.cards.cards.flashcards.domain.*;
import com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest;
import com.ten.devs.cards.cards.flashcards.presentation.request.ApproveAiSuggestionsRequest.ApprovedSuggestion;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.ApproveAiSuggestionsResponse.CreatedFlashcard;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveAiSuggestionsCommandHandler")
class ApproveAiSuggestionsCommandHandlerTest {

    @Mock
    private AiGenerationSessionRepository sessionRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @InjectMocks
    private ApproveAiSuggestionsCommandHandler handler;

    private static final UUID TEST_SESSION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OTHER_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
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

    private AiGenerationSession createCompletedSession(List<FlashcardSuggestion> suggestions) {
        return AiGenerationSession.createCompleted(
            TEST_SESSION_ID,
            TEST_USER_ID,
            TEST_INPUT_TEXT,
            suggestions,
            "openai/gpt-4o-mini",
            new BigDecimal("0.05")
        );
    }

    @Nested
    @DisplayName("Successful approval")
    class SuccessfulApproval {

        @Test
        @DisplayName("Given valid unmodified suggestions, When approving, Then should create flashcards with AI source")
        void givenValidUnmodifiedSuggestions_whenApproving_thenShouldCreateFlashcardsWithAiSource() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null),
                new ApprovedSuggestion(suggestions.get(1).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.createdFlashcards()).hasSize(2);
            assertThat(response.createdFlashcards())
                .allMatch(fc -> fc.source().equals(FlashcardSource.AI.name()));
        }

        @Test
        @DisplayName("Given modified suggestions, When approving, Then should create flashcards with AI_USER source")
        void givenModifiedSuggestions_whenApproving_thenShouldCreateFlashcardsWithAiUserSource() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), "Modified Question", null),
                new ApprovedSuggestion(suggestions.get(1).id(), null, "Modified Answer")
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.createdFlashcards()).hasSize(2);
            assertThat(response.createdFlashcards())
                .allMatch(fc -> fc.source().equals(FlashcardSource.AI_USER.name()));
        }

        @Test
        @DisplayName("Given mixed modified and unmodified suggestions, When approving, Then should assign correct sources")
        void givenMixedModifiedAndUnmodifiedSuggestions_whenApproving_thenShouldAssignCorrectSources() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(4);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null), // AI
                new ApprovedSuggestion(suggestions.get(1).id(), "Modified", null), // AI_USER
                new ApprovedSuggestion(suggestions.get(2).id(), null, "Modified"), // AI_USER
                new ApprovedSuggestion(suggestions.get(3).id(), "Mod Front", "Mod Back") // AI_USER
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            ArgumentCaptor<List<Flashcard>> flashcardCaptor = ArgumentCaptor.forClass(List.class);
            when(flashcardRepository.saveAll(flashcardCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            List<Flashcard> savedFlashcards = flashcardCaptor.getValue();
            assertThat(savedFlashcards.get(0).toSnapshot().source()).isEqualTo(FlashcardSource.AI);
            assertThat(savedFlashcards.get(1).toSnapshot().source()).isEqualTo(FlashcardSource.AI_USER);
            assertThat(savedFlashcards.get(2).toSnapshot().source()).isEqualTo(FlashcardSource.AI_USER);
            assertThat(savedFlashcards.get(3).toSnapshot().source()).isEqualTo(FlashcardSource.AI_USER);
        }

        @Test
        @DisplayName("Given approved suggestions, When saving, Then should update session accepted count")
        void givenApprovedSuggestions_whenSaving_thenShouldUpdateSessionAcceptedCount() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null),
                new ApprovedSuggestion(suggestions.get(1).id(), null, null),
                new ApprovedSuggestion(suggestions.get(2).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<AiGenerationSession> sessionCaptor = ArgumentCaptor.forClass(AiGenerationSession.class);
            when(sessionRepository.save(sessionCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.handle(command);

            // Then
            AiGenerationSession updatedSession = sessionCaptor.getValue();
            assertThat(updatedSession.toSnapshot().acceptedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Given valid request, When approving, Then should verify ownership")
        void givenValidRequest_whenApproving_thenShouldVerifyOwnership() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(2);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.handle(command);

            // Then - no exception thrown means ownership verified
            verify(sessionRepository, times(1)).findById(TEST_SESSION_ID);
        }

        @Test
        @DisplayName("Given all suggestions approved, When handling, Then should create flashcards for all")
        void givenAllSuggestionsApproved_whenHandling_thenShouldCreateFlashcardsForAll() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null),
                new ApprovedSuggestion(suggestions.get(1).id(), null, null),
                new ApprovedSuggestion(suggestions.get(2).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.createdFlashcards()).hasSize(3);
        }

        @Test
        @DisplayName("Given partial approval, When handling, Then should create flashcards only for approved")
        void givenPartialApproval_whenHandling_thenShouldCreateFlashcardsOnlyForApproved() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = createCompletedSession(suggestions);

            // Only approve 2 out of 5 suggestions
            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(1).id(), null, null),
                new ApprovedSuggestion(suggestions.get(3).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.createdFlashcards()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Error cases")
    class ErrorCases {

        @Test
        @DisplayName("Given non-existent session, When approving, Then should throw IllegalArgumentException")
        void givenNonExistentSession_whenApproving_thenShouldThrowIllegalArgumentException() {
            // Given
            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(UUID.randomUUID(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Session not found: " + TEST_SESSION_ID);
        }

        @Test
        @DisplayName("Given session owned by different user, When approving, Then should throw IllegalArgumentException")
        void givenSessionOwnedByDifferentUser_whenApproving_thenShouldThrowIllegalArgumentException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(2);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                OTHER_USER_ID, // Different user
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not owned by user");
        }

        @Test
        @DisplayName("Given PENDING session, When approving, Then should throw IllegalStateException")
        void givenPendingSession_whenApproving_thenShouldThrowIllegalStateException() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, TEST_INPUT_TEXT);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(UUID.randomUUID(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot approve suggestions for session in status");
        }

        @Test
        @DisplayName("Given FAILED session, When approving, Then should throw IllegalStateException")
        void givenFailedSession_whenApproving_thenShouldThrowIllegalStateException() {
            // Given
            AiGenerationSession session = AiGenerationSession.createFailed(
                TEST_SESSION_ID,
                TEST_USER_ID,
                TEST_INPUT_TEXT
            );

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(UUID.randomUUID(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot approve suggestions for session in status");
        }

        @Test
        @DisplayName("Given non-existent suggestion ID, When approving, Then should throw IllegalArgumentException")
        void givenNonExistentSuggestionId_whenApproving_thenShouldThrowIllegalArgumentException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(2);
            AiGenerationSession session = createCompletedSession(suggestions);

            UUID nonExistentId = UUID.randomUUID();
            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(nonExistentId, null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Suggestion not found in session: " + nonExistentId);
        }

        @Test
        @DisplayName("Given flashcard repository throws exception, When saving, Then should propagate exception")
        void givenFlashcardRepositoryThrowsException_whenSaving_thenShouldPropagateException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(2);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("Database connection error"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");
        }

        @Test
        @DisplayName("Given session repository throws exception when updating, When saving, Then should propagate exception")
        void givenSessionRepositoryThrowsExceptionWhenUpdating_whenSaving_thenShouldPropagateException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(2);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenThrow(new RuntimeException("Session update failed"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Session update failed");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Given content with maximum allowed length, When approving, Then should create flashcard successfully")
        void givenContentWithMaximumAllowedLength_whenApproving_thenShouldCreateFlashcardSuccessfully() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(1);
            AiGenerationSession session = createCompletedSession(suggestions);

            String maxContent = "A".repeat(1000); // Maximum allowed length
            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), maxContent, maxContent)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            assertThat(response.createdFlashcards()).hasSize(1);
            assertThat(response.createdFlashcards().get(0).frontContent()).hasSize(1000);
            assertThat(response.createdFlashcards().get(0).backContent()).hasSize(1000);
        }

        @Test
        @DisplayName("Given single suggestion approved, When handling, Then should update accepted count to 1")
        void givenSingleSuggestionApproved_whenHandling_thenShouldUpdateAcceptedCountToOne() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(2).id(), null, null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<AiGenerationSession> sessionCaptor = ArgumentCaptor.forClass(AiGenerationSession.class);
            when(sessionRepository.save(sessionCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.handle(command);

            // Then
            assertThat(sessionCaptor.getValue().toSnapshot().acceptedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Given content with special characters, When approving, Then should preserve content exactly")
        void givenContentWithSpecialCharacters_whenApproving_thenShouldPreserveContentExactly() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(1);
            AiGenerationSession session = createCompletedSession(suggestions);

            String specialFront = "What is \"polymorphism\"?";
            String specialBack = "It's <concept> with & symbols!";

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestions.get(0).id(), specialFront, specialBack)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            CreatedFlashcard created = response.createdFlashcards().get(0);
            assertThat(created.frontContent()).isEqualTo(specialFront);
            assertThat(created.backContent()).isEqualTo(specialBack);
        }

        @Test
        @DisplayName("Given only frontContent modified, When approving, Then should use original backContent")
        void givenOnlyFrontContentModified_whenApproving_thenShouldUseOriginalBackContent() {
            // Given
            FlashcardSuggestion suggestion = new FlashcardSuggestion(
                UUID.randomUUID(),
                TEST_SESSION_ID,
                "Original Front",
                "Original Back"
            );
            List<FlashcardSuggestion> suggestions = List.of(suggestion);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestion.id(), "Modified Front", null)
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            CreatedFlashcard created = response.createdFlashcards().get(0);
            assertThat(created.frontContent()).isEqualTo("Modified Front");
            assertThat(created.backContent()).isEqualTo("Original Back");
            assertThat(created.source()).isEqualTo(FlashcardSource.AI_USER.name());
        }

        @Test
        @DisplayName("Given only backContent modified, When approving, Then should use original frontContent")
        void givenOnlyBackContentModified_whenApproving_thenShouldUseOriginalFrontContent() {
            // Given
            FlashcardSuggestion suggestion = new FlashcardSuggestion(
                UUID.randomUUID(),
                TEST_SESSION_ID,
                "Original Front",
                "Original Back"
            );
            List<FlashcardSuggestion> suggestions = List.of(suggestion);
            AiGenerationSession session = createCompletedSession(suggestions);

            List<ApprovedSuggestion> approvedSuggestions = List.of(
                new ApprovedSuggestion(suggestion.id(), null, "Modified Back")
            );
            ApproveAiSuggestionsRequest request = new ApproveAiSuggestionsRequest(approvedSuggestions);

            ApproveAiSuggestionsCommand command = new ApproveAiSuggestionsCommand(
                TEST_USER_ID,
                TEST_SESSION_ID,
                request
            );

            when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
            when(flashcardRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(sessionRepository.save(any(AiGenerationSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApproveAiSuggestionsResponse response = handler.handle(command);

            // Then
            CreatedFlashcard created = response.createdFlashcards().get(0);
            assertThat(created.frontContent()).isEqualTo("Original Front");
            assertThat(created.backContent()).isEqualTo("Modified Back");
            assertThat(created.source()).isEqualTo(FlashcardSource.AI_USER.name());
        }
    }
}
