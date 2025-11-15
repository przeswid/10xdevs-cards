package com.ten.devs.cards.cards.flashcards.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiGenerationSession Domain Entity")
class AiGenerationSessionTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_SESSION_ID = UUID.randomUUID();
    private static final String VALID_INPUT_TEXT = "A".repeat(1000); // Minimum valid length
    private static final String AI_MODEL = "gpt-4";
    private static final BigDecimal API_COST = new BigDecimal("0.05");

    private static List<FlashcardSuggestion> createTestSuggestions(int count) {
        List<FlashcardSuggestion> suggestions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            suggestions.add(new FlashcardSuggestion(
                UUID.randomUUID(),  // id
                TEST_SESSION_ID,     // sessionId
                "Question " + i,     // frontContent
                "Answer " + i        // backContent
            ));
        }
        return suggestions;
    }

    @Nested
    @DisplayName("create() factory method")
    class CreateMethod {

        @Test
        @DisplayName("Given valid user ID and input text, When creating session, Then should create PENDING session with empty suggestions")
        void givenValidInputs_whenCreating_thenShouldCreatePendingSession() {
            // When
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // Then
            assertThat(session).isNotNull();
            assertThat(session.isPending()).isTrue();
            assertThat(session.getSuggestions()).isEmpty();
            assertThat(session.toSnapshot().userId()).isEqualTo(TEST_USER_ID);
            assertThat(session.toSnapshot().inputText()).isEqualTo(VALID_INPUT_TEXT);
            assertThat(session.toSnapshot().generatedCount()).isZero();
            assertThat(session.toSnapshot().acceptedCount()).isZero();
        }

        @Test
        @DisplayName("Given null input text, When creating session, Then should throw IllegalArgumentException")
        void givenNullInputText_whenCreating_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.create(TEST_USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input text cannot be null or empty");
        }

        @Test
        @DisplayName("Given blank input text, When creating session, Then should throw IllegalArgumentException")
        void givenBlankInputText_whenCreating_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.create(TEST_USER_ID, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input text cannot be null or empty");
        }

        @Test
        @DisplayName("Given input text less than 1000 characters, When creating session, Then should throw IllegalArgumentException")
        void givenTooShortInputText_whenCreating_thenShouldThrowException() {
            // Given
            String shortText = "A".repeat(999);

            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.create(TEST_USER_ID, shortText))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input text must be between 1000 and 10000 characters");
        }

        @Test
        @DisplayName("Given input text more than 10000 characters, When creating session, Then should throw IllegalArgumentException")
        void givenTooLongInputText_whenCreating_thenShouldThrowException() {
            // Given
            String longText = "A".repeat(10001);

            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.create(TEST_USER_ID, longText))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input text must be between 1000 and 10000 characters");
        }

        @Test
        @DisplayName("Given input text exactly 1000 characters, When creating session, Then should create successfully")
        void givenMinimumLengthInputText_whenCreating_thenShouldCreateSuccessfully() {
            // Given
            String minText = "A".repeat(1000);

            // When
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, minText);

            // Then
            assertThat(session).isNotNull();
            assertThat(session.toSnapshot().inputText()).hasSize(1000);
        }

        @Test
        @DisplayName("Given input text exactly 10000 characters, When creating session, Then should create successfully")
        void givenMaximumLengthInputText_whenCreating_thenShouldCreateSuccessfully() {
            // Given
            String maxText = "A".repeat(10000);

            // When
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, maxText);

            // Then
            assertThat(session).isNotNull();
            assertThat(session.toSnapshot().inputText()).hasSize(10000);
        }
    }

    @Nested
    @DisplayName("createCompleted() factory method")
    class CreateCompletedMethod {

        @Test
        @DisplayName("Given valid inputs with suggestions, When creating completed session, Then should create COMPLETED session with suggestions")
        void givenValidInputsWithSuggestions_whenCreatingCompleted_thenShouldCreateCompletedSession() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);

            // When
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // Then
            assertThat(session).isNotNull();
            assertThat(session.isCompleted()).isTrue();
            assertThat(session.canProvideSuggestions()).isTrue();
            assertThat(session.getSuggestions()).hasSize(5);
            assertThat(session.toSnapshot().id()).isEqualTo(TEST_SESSION_ID);
            assertThat(session.toSnapshot().generatedCount()).isEqualTo(5);
            assertThat(session.toSnapshot().aiModel()).isEqualTo(AI_MODEL);
            assertThat(session.toSnapshot().apiCost()).isEqualTo(API_COST);
        }

        @Test
        @DisplayName("Given null suggestions, When creating completed session, Then should throw IllegalArgumentException")
        void givenNullSuggestions_whenCreatingCompleted_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, null, AI_MODEL, API_COST
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create completed session without suggestions");
        }

        @Test
        @DisplayName("Given empty suggestions list, When creating completed session, Then should throw IllegalArgumentException")
        void givenEmptySuggestions_whenCreatingCompleted_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, List.of(), AI_MODEL, API_COST
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create completed session without suggestions");
        }

        @Test
        @DisplayName("Given invalid input text, When creating completed session, Then should throw IllegalArgumentException")
        void givenInvalidInputText_whenCreatingCompleted_thenShouldThrowException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            String shortText = "A".repeat(500);

            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, shortText, suggestions, AI_MODEL, API_COST
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input text must be between 1000 and 10000 characters");
        }
    }

    @Nested
    @DisplayName("createFailed() factory method")
    class CreateFailedMethod {

        @Test
        @DisplayName("Given valid inputs, When creating failed session, Then should create FAILED session")
        void givenValidInputs_whenCreatingFailed_thenShouldCreateFailedSession() {
            // When
            AiGenerationSession session = AiGenerationSession.createFailed(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT
            );

            // Then
            assertThat(session).isNotNull();
            assertThat(session.hasFailed()).isTrue();
            assertThat(session.canProvideSuggestions()).isFalse();
            assertThat(session.getSuggestions()).isEmpty();
            assertThat(session.toSnapshot().id()).isEqualTo(TEST_SESSION_ID);
            assertThat(session.toSnapshot().generatedCount()).isZero();
            assertThat(session.toSnapshot().aiModel()).isNull();
            assertThat(session.toSnapshot().apiCost()).isNull();
        }

        @Test
        @DisplayName("Given invalid input text, When creating failed session, Then should throw IllegalArgumentException")
        void givenInvalidInputText_whenCreatingFailed_thenShouldThrowException() {
            // Given
            String shortText = "Too short";

            // When & Then
            assertThatThrownBy(() -> AiGenerationSession.createFailed(
                TEST_SESSION_ID, TEST_USER_ID, shortText
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input text must be between 1000 and 10000 characters");
        }
    }

    @Nested
    @DisplayName("complete() business method")
    class CompleteMethod {

        @Test
        @DisplayName("Given PENDING session with valid suggestions, When completing, Then should transition to COMPLETED status")
        void givenPendingSessionWithSuggestions_whenCompleting_thenShouldTransitionToCompleted() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);

            // When
            session.complete(suggestions, AI_MODEL, API_COST);

            // Then
            assertThat(session.isCompleted()).isTrue();
            assertThat(session.canProvideSuggestions()).isTrue();
            assertThat(session.getSuggestions()).hasSize(3);
            assertThat(session.toSnapshot().generatedCount()).isEqualTo(3);
            assertThat(session.toSnapshot().aiModel()).isEqualTo(AI_MODEL);
            assertThat(session.toSnapshot().apiCost()).isEqualTo(API_COST);
        }

        @Test
        @DisplayName("Given COMPLETED session, When attempting to complete again, Then should throw IllegalStateException")
        void givenCompletedSession_whenCompletingAgain_thenShouldThrowException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When & Then
            assertThatThrownBy(() -> session.complete(suggestions, AI_MODEL, API_COST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only complete PENDING sessions");
        }

        @Test
        @DisplayName("Given FAILED session, When attempting to complete, Then should throw IllegalStateException")
        void givenFailedSession_whenCompleting_thenShouldThrowException() {
            // Given
            AiGenerationSession session = AiGenerationSession.createFailed(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT
            );
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);

            // When & Then
            assertThatThrownBy(() -> session.complete(suggestions, AI_MODEL, API_COST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only complete PENDING sessions");
        }

        @Test
        @DisplayName("Given null suggestions, When completing, Then should throw IllegalArgumentException")
        void givenNullSuggestions_whenCompleting_thenShouldThrowException() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // When & Then
            assertThatThrownBy(() -> session.complete(null, AI_MODEL, API_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot complete session without suggestions");
        }

        @Test
        @DisplayName("Given empty suggestions list, When completing, Then should throw IllegalArgumentException")
        void givenEmptySuggestions_whenCompleting_thenShouldThrowException() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // When & Then
            assertThatThrownBy(() -> session.complete(List.of(), AI_MODEL, API_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot complete session without suggestions");
        }

        @Test
        @DisplayName("Given suggestions list, When completing, Then should create defensive copy")
        void givenSuggestionsList_whenCompleting_thenShouldCreateDefensiveCopy() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);
            List<FlashcardSuggestion> suggestions = new ArrayList<>(createTestSuggestions(3));

            // When
            session.complete(suggestions, AI_MODEL, API_COST);
            suggestions.clear(); // Modify original list

            // Then
            assertThat(session.getSuggestions()).hasSize(3); // Session copy unaffected
        }
    }

    @Nested
    @DisplayName("fail() business method")
    class FailMethod {

        @Test
        @DisplayName("Given PENDING session, When failing, Then should transition to FAILED status")
        void givenPendingSession_whenFailing_thenShouldTransitionToFailed() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // When
            session.fail();

            // Then
            assertThat(session.hasFailed()).isTrue();
            assertThat(session.canProvideSuggestions()).isFalse();
            assertThat(session.getSuggestions()).isEmpty();
        }

        @Test
        @DisplayName("Given COMPLETED session, When attempting to fail, Then should throw IllegalStateException")
        void givenCompletedSession_whenFailing_thenShouldThrowException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When & Then
            assertThatThrownBy(session::fail)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only fail PENDING sessions");
        }

        @Test
        @DisplayName("Given FAILED session, When attempting to fail again, Then should throw IllegalStateException")
        void givenFailedSession_whenFailingAgain_thenShouldThrowException() {
            // Given
            AiGenerationSession session = AiGenerationSession.createFailed(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT
            );

            // When & Then
            assertThatThrownBy(session::fail)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only fail PENDING sessions");
        }
    }

    @Nested
    @DisplayName("updateAcceptedCount() business method")
    class UpdateAcceptedCountMethod {

        @Test
        @DisplayName("Given COMPLETED session with valid accepted count, When updating, Then should update accepted count")
        void givenCompletedSessionWithValidCount_whenUpdating_thenShouldUpdateCount() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When
            session.updateAcceptedCount(3);

            // Then
            assertThat(session.toSnapshot().acceptedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Given COMPLETED session, When updating with zero accepted count, Then should allow zero")
        void givenCompletedSession_whenUpdatingWithZero_thenShouldAllowZero() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When
            session.updateAcceptedCount(0);

            // Then
            assertThat(session.toSnapshot().acceptedCount()).isZero();
        }

        @Test
        @DisplayName("Given COMPLETED session with 5 suggestions, When updating with 5 accepted, Then should allow maximum")
        void givenCompletedSession_whenUpdatingWithMaximum_thenShouldAllowMaximum() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When
            session.updateAcceptedCount(5);

            // Then
            assertThat(session.toSnapshot().acceptedCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Given PENDING session, When updating accepted count, Then should throw IllegalStateException")
        void givenPendingSession_whenUpdatingCount_thenShouldThrowException() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // When & Then
            assertThatThrownBy(() -> session.updateAcceptedCount(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only update accepted count for COMPLETED sessions");
        }

        @Test
        @DisplayName("Given negative accepted count, When updating, Then should throw IllegalArgumentException")
        void givenNegativeCount_whenUpdating_thenShouldThrowException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When & Then
            assertThatThrownBy(() -> session.updateAcceptedCount(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Accepted count must be between 0 and 5");
        }

        @Test
        @DisplayName("Given accepted count greater than generated count, When updating, Then should throw IllegalArgumentException")
        void givenCountGreaterThanGenerated_whenUpdating_thenShouldThrowException() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(5);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When & Then
            assertThatThrownBy(() -> session.updateAcceptedCount(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Accepted count must be between 0 and 5");
        }
    }

    @Nested
    @DisplayName("ensureOwnedBy() business method")
    class EnsureOwnedByMethod {

        @Test
        @DisplayName("Given session owned by user, When ensuring ownership, Then should not throw exception")
        void givenSessionOwnedByUser_whenEnsuringOwnership_thenShouldNotThrow() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // When & Then
            assertThatCode(() -> session.ensureOwnedBy(TEST_USER_ID))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Given session owned by different user, When ensuring ownership, Then should throw IllegalArgumentException")
        void givenSessionOwnedByDifferentUser_whenEnsuringOwnership_thenShouldThrowException() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);
            UUID otherUserId = UUID.randomUUID();

            // When & Then
            assertThatThrownBy(() -> session.ensureOwnedBy(otherUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not owned by user");
        }
    }

    @Nested
    @DisplayName("isOwnedBy() business query")
    class IsOwnedByMethod {

        @Test
        @DisplayName("Given session owned by user, When checking ownership, Then should return true")
        void givenSessionOwnedByUser_whenCheckingOwnership_thenShouldReturnTrue() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // When
            boolean isOwned = session.isOwnedBy(TEST_USER_ID);

            // Then
            assertThat(isOwned).isTrue();
        }

        @Test
        @DisplayName("Given session owned by different user, When checking ownership, Then should return false")
        void givenSessionOwnedByDifferentUser_whenCheckingOwnership_thenShouldReturnFalse() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);
            UUID otherUserId = UUID.randomUUID();

            // When
            boolean isOwned = session.isOwnedBy(otherUserId);

            // Then
            assertThat(isOwned).isFalse();
        }
    }

    @Nested
    @DisplayName("canProvideSuggestions() business query")
    class CanProvideSuggestionsMethod {

        @Test
        @DisplayName("Given COMPLETED session, When checking if can provide suggestions, Then should return true")
        void givenCompletedSession_whenCheckingCanProvideSuggestions_thenShouldReturnTrue() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When
            boolean canProvide = session.canProvideSuggestions();

            // Then
            assertThat(canProvide).isTrue();
        }

        @Test
        @DisplayName("Given PENDING session, When checking if can provide suggestions, Then should return false")
        void givenPendingSession_whenCheckingCanProvideSuggestions_thenShouldReturnFalse() {
            // Given
            AiGenerationSession session = AiGenerationSession.create(TEST_USER_ID, VALID_INPUT_TEXT);

            // When
            boolean canProvide = session.canProvideSuggestions();

            // Then
            assertThat(canProvide).isFalse();
        }

        @Test
        @DisplayName("Given FAILED session, When checking if can provide suggestions, Then should return false")
        void givenFailedSession_whenCheckingCanProvideSuggestions_thenShouldReturnFalse() {
            // Given
            AiGenerationSession session = AiGenerationSession.createFailed(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT
            );

            // When
            boolean canProvide = session.canProvideSuggestions();

            // Then
            assertThat(canProvide).isFalse();
        }
    }

    @Nested
    @DisplayName("getSuggestions() business query")
    class GetSuggestionsMethod {

        @Test
        @DisplayName("Given session with suggestions, When getting suggestions, Then should return immutable copy")
        void givenSessionWithSuggestions_whenGettingSuggestions_thenShouldReturnImmutableCopy() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When
            List<FlashcardSuggestion> retrievedSuggestions = session.getSuggestions();

            // Then
            assertThat(retrievedSuggestions).hasSize(3);
            assertThatThrownBy(() -> retrievedSuggestions.clear())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("toSnapshot() snapshot pattern")
    class ToSnapshotMethod {

        @Test
        @DisplayName("Given session, When converting to snapshot, Then should capture all state")
        void givenSession_whenConvertingToSnapshot_thenShouldCaptureAllState() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When
            AiGenerationSessionSnapshot snapshot = session.toSnapshot();

            // Then
            assertThat(snapshot.id()).isEqualTo(TEST_SESSION_ID);
            assertThat(snapshot.userId()).isEqualTo(TEST_USER_ID);
            assertThat(snapshot.inputText()).isEqualTo(VALID_INPUT_TEXT);
            assertThat(snapshot.suggestions()).hasSize(3);
            assertThat(snapshot.generatedCount()).isEqualTo(3);
            assertThat(snapshot.acceptedCount()).isZero();
            assertThat(snapshot.aiModel()).isEqualTo(AI_MODEL);
            assertThat(snapshot.apiCost()).isEqualTo(API_COST);
            assertThat(snapshot.status()).isEqualTo(AiGenerationSessionStatus.COMPLETED);
            assertThat(snapshot.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("Given session, When converting to snapshot, Then snapshot should be immutable")
        void givenSession_whenConvertingToSnapshot_thenSnapshotShouldBeImmutable() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSession session = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When
            AiGenerationSessionSnapshot snapshot = session.toSnapshot();

            // Then - snapshot suggestions should be immutable
            assertThatThrownBy(() -> snapshot.suggestions().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("fromSnapshot() factory method")
    class FromSnapshotMethod {

        @Test
        @DisplayName("Given valid snapshot, When reconstructing session, Then should restore all state")
        void givenValidSnapshot_whenReconstructing_thenShouldRestoreAllState() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);
            AiGenerationSessionSnapshot snapshot = AiGenerationSessionSnapshot.builder()
                .id(TEST_SESSION_ID)
                .userId(TEST_USER_ID)
                .inputText(VALID_INPUT_TEXT)
                .suggestions(suggestions)
                .generatedCount(3)
                .acceptedCount(2)
                .aiModel(AI_MODEL)
                .apiCost(API_COST)
                .status(AiGenerationSessionStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();

            // When
            AiGenerationSession session = AiGenerationSession.fromSnapshot(snapshot);

            // Then
            AiGenerationSessionSnapshot reconstructed = session.toSnapshot();
            assertThat(reconstructed.id()).isEqualTo(TEST_SESSION_ID);
            assertThat(reconstructed.userId()).isEqualTo(TEST_USER_ID);
            assertThat(reconstructed.suggestions()).hasSize(3);
            assertThat(reconstructed.generatedCount()).isEqualTo(3);
            assertThat(reconstructed.acceptedCount()).isEqualTo(2);
            assertThat(reconstructed.status()).isEqualTo(AiGenerationSessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("Given snapshot with null suggestions, When reconstructing, Then should handle gracefully")
        void givenSnapshotWithNullSuggestions_whenReconstructing_thenShouldHandleGracefully() {
            // Given
            AiGenerationSessionSnapshot snapshot = AiGenerationSessionSnapshot.builder()
                .id(TEST_SESSION_ID)
                .userId(TEST_USER_ID)
                .inputText(VALID_INPUT_TEXT)
                .suggestions(null) // Null suggestions
                .generatedCount(null)
                .acceptedCount(null)
                .aiModel(null)
                .apiCost(null)
                .status(AiGenerationSessionStatus.PENDING)
                .createdAt(Instant.now())
                .build();

            // When
            AiGenerationSession session = AiGenerationSession.fromSnapshot(snapshot);

            // Then
            assertThat(session.getSuggestions()).isEmpty();
            assertThat(session.toSnapshot().generatedCount()).isZero();
            assertThat(session.toSnapshot().acceptedCount()).isZero();
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCodeMethod {

        @Test
        @DisplayName("Given two sessions with same ID, When comparing, Then should be equal")
        void givenTwoSessionsWithSameId_whenComparing_thenShouldBeEqual() {
            // Given
            List<FlashcardSuggestion> suggestions1 = createTestSuggestions(3);
            List<FlashcardSuggestion> suggestions2 = createTestSuggestions(5);

            AiGenerationSession session1 = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions1, AI_MODEL, API_COST
            );
            AiGenerationSession session2 = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, UUID.randomUUID(), VALID_INPUT_TEXT, suggestions2, "gpt-3.5", BigDecimal.ONE
            );

            // When & Then
            assertThat(session1).isEqualTo(session2);
            assertThat(session1.hashCode()).isEqualTo(session2.hashCode());
        }

        @Test
        @DisplayName("Given two sessions with different IDs, When comparing, Then should not be equal")
        void givenTwoSessionsWithDifferentIds_whenComparing_thenShouldNotBeEqual() {
            // Given
            List<FlashcardSuggestion> suggestions = createTestSuggestions(3);

            AiGenerationSession session1 = AiGenerationSession.createCompleted(
                TEST_SESSION_ID, TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );
            AiGenerationSession session2 = AiGenerationSession.createCompleted(
                UUID.randomUUID(), TEST_USER_ID, VALID_INPUT_TEXT, suggestions, AI_MODEL, API_COST
            );

            // When & Then
            assertThat(session1).isNotEqualTo(session2);
        }
    }
}
