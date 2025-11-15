package com.ten.devs.cards.cards.flashcards.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Flashcard Domain Entity")
class FlashcardTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_SESSION_ID = UUID.randomUUID();
    private static final String VALID_FRONT_CONTENT = "What is Domain-Driven Design?";
    private static final String VALID_BACK_CONTENT = "DDD is an approach to software development focusing on the domain model.";

    @Nested
    @DisplayName("createFromAiSuggestion() factory method")
    class CreateFromAiSuggestionMethod {

        @Test
        @DisplayName("Given valid inputs with AI source, When creating from AI suggestion, Then should create flashcard with AI source")
        void givenValidInputsWithAiSource_whenCreatingFromAiSuggestion_thenShouldCreateWithAiSource() {
            // When
            Flashcard flashcard = Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.AI, TEST_SESSION_ID
            );

            // Then
            assertThat(flashcard).isNotNull();
            assertThat(flashcard.isAiGenerated()).isTrue();
            assertThat(flashcard.toSnapshot().source()).isEqualTo(FlashcardSource.AI);
            assertThat(flashcard.toSnapshot().userId()).isEqualTo(TEST_USER_ID);
            assertThat(flashcard.toSnapshot().frontContent()).isEqualTo(VALID_FRONT_CONTENT);
            assertThat(flashcard.toSnapshot().backContent()).isEqualTo(VALID_BACK_CONTENT);
            assertThat(flashcard.toSnapshot().generationSessionId()).isEqualTo(TEST_SESSION_ID);
            assertThat(flashcard.toSnapshot().id()).isNotNull();
            assertThat(flashcard.toSnapshot().createdAt()).isNotNull();
            assertThat(flashcard.toSnapshot().updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Given valid inputs with AI_USER source, When creating from AI suggestion, Then should create flashcard with AI_USER source")
        void givenValidInputsWithAiUserSource_whenCreatingFromAiSuggestion_thenShouldCreateWithAiUserSource() {
            // When
            Flashcard flashcard = Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.AI_USER, TEST_SESSION_ID
            );

            // Then
            assertThat(flashcard).isNotNull();
            assertThat(flashcard.isAiGenerated()).isTrue();
            assertThat(flashcard.toSnapshot().source()).isEqualTo(FlashcardSource.AI_USER);
        }

        @Test
        @DisplayName("Given USER source, When creating from AI suggestion, Then should throw IllegalArgumentException")
        void givenUserSource_whenCreatingFromAiSuggestion_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.USER, TEST_SESSION_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI-generated flashcards must have source AI or AI_USER");
        }

        @Test
        @DisplayName("Given null generation session ID, When creating from AI suggestion, Then should throw IllegalArgumentException")
        void givenNullGenerationSessionId_whenCreatingFromAiSuggestion_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.AI, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI-generated flashcards must have generation session ID");
        }

        @Test
        @DisplayName("Given null front content, When creating from AI suggestion, Then should throw IllegalArgumentException")
        void givenNullFrontContent_whenCreatingFromAiSuggestion_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Flashcard.createFromAiSuggestion(
                TEST_USER_ID, null, VALID_BACK_CONTENT, FlashcardSource.AI, TEST_SESSION_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Front content cannot be null or empty");
        }

        @Test
        @DisplayName("Given blank front content, When creating from AI suggestion, Then should throw IllegalArgumentException")
        void givenBlankFrontContent_whenCreatingFromAiSuggestion_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Flashcard.createFromAiSuggestion(
                TEST_USER_ID, "   ", VALID_BACK_CONTENT, FlashcardSource.AI, TEST_SESSION_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Front content cannot be null or empty");
        }

        @Test
        @DisplayName("Given null back content, When creating from AI suggestion, Then should throw IllegalArgumentException")
        void givenNullBackContent_whenCreatingFromAiSuggestion_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, null, FlashcardSource.AI, TEST_SESSION_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Back content cannot be null or empty");
        }

        @Test
        @DisplayName("Given front content exceeding 1000 characters, When creating from AI suggestion, Then should throw IllegalArgumentException")
        void givenTooLongFrontContent_whenCreatingFromAiSuggestion_thenShouldThrowException() {
            // Given
            String longContent = "A".repeat(1001);

            // When & Then
            assertThatThrownBy(() -> Flashcard.createFromAiSuggestion(
                TEST_USER_ID, longContent, VALID_BACK_CONTENT, FlashcardSource.AI, TEST_SESSION_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Front content cannot exceed 1000 characters");
        }

        @Test
        @DisplayName("Given back content exceeding 1000 characters, When creating from AI suggestion, Then should throw IllegalArgumentException")
        void givenTooLongBackContent_whenCreatingFromAiSuggestion_thenShouldThrowException() {
            // Given
            String longContent = "A".repeat(1001);

            // When & Then
            assertThatThrownBy(() -> Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, longContent, FlashcardSource.AI, TEST_SESSION_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Back content cannot exceed 1000 characters");
        }

        @Test
        @DisplayName("Given content exactly 1000 characters, When creating from AI suggestion, Then should create successfully")
        void givenMaximumLengthContent_whenCreatingFromAiSuggestion_thenShouldCreateSuccessfully() {
            // Given
            String maxContent = "A".repeat(1000);

            // When
            Flashcard flashcard = Flashcard.createFromAiSuggestion(
                TEST_USER_ID, maxContent, maxContent, FlashcardSource.AI, TEST_SESSION_ID
            );

            // Then
            assertThat(flashcard).isNotNull();
            assertThat(flashcard.toSnapshot().frontContent()).hasSize(1000);
            assertThat(flashcard.toSnapshot().backContent()).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("createManual() factory method")
    class CreateManualMethod {

        @Test
        @DisplayName("Given valid inputs, When creating manual flashcard, Then should create flashcard with USER source")
        void givenValidInputs_whenCreatingManual_thenShouldCreateWithUserSource() {
            // When
            Flashcard flashcard = Flashcard.createManual(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT
            );

            // Then
            assertThat(flashcard).isNotNull();
            assertThat(flashcard.isAiGenerated()).isFalse();
            assertThat(flashcard.toSnapshot().source()).isEqualTo(FlashcardSource.USER);
            assertThat(flashcard.toSnapshot().userId()).isEqualTo(TEST_USER_ID);
            assertThat(flashcard.toSnapshot().frontContent()).isEqualTo(VALID_FRONT_CONTENT);
            assertThat(flashcard.toSnapshot().backContent()).isEqualTo(VALID_BACK_CONTENT);
            assertThat(flashcard.toSnapshot().generationSessionId()).isNull();
            assertThat(flashcard.toSnapshot().id()).isNotNull();
            assertThat(flashcard.toSnapshot().createdAt()).isNotNull();
        }

        @Test
        @DisplayName("Given null front content, When creating manual flashcard, Then should throw IllegalArgumentException")
        void givenNullFrontContent_whenCreatingManual_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Flashcard.createManual(TEST_USER_ID, null, VALID_BACK_CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Front content cannot be null or empty");
        }

        @Test
        @DisplayName("Given invalid content, When creating manual flashcard, Then should throw IllegalArgumentException")
        void givenInvalidContent_whenCreatingManual_thenShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Flashcard.createManual(TEST_USER_ID, "", VALID_BACK_CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Front content cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("updateContent() business method")
    class UpdateContentMethod {

        @Test
        @DisplayName("Given flashcard with valid new content, When updating content, Then should update content and timestamp")
        void givenFlashcardWithValidNewContent_whenUpdatingContent_thenShouldUpdateContentAndTimestamp() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);
            FlashcardSnapshot originalSnapshot = flashcard.toSnapshot();
            String newFront = "Updated front content";
            String newBack = "Updated back content";

            // When
            flashcard.updateContent(newFront, newBack);

            // Then
            FlashcardSnapshot updatedSnapshot = flashcard.toSnapshot();
            assertThat(updatedSnapshot.frontContent()).isEqualTo(newFront);
            assertThat(updatedSnapshot.backContent()).isEqualTo(newBack);
            assertThat(updatedSnapshot.updatedAt()).isAfter(originalSnapshot.updatedAt());
            assertThat(updatedSnapshot.createdAt()).isEqualTo(originalSnapshot.createdAt());
        }

        @Test
        @DisplayName("Given null front content, When updating content, Then should throw IllegalArgumentException")
        void givenNullFrontContent_whenUpdatingContent_thenShouldThrowException() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);

            // When & Then
            assertThatThrownBy(() -> flashcard.updateContent(null, VALID_BACK_CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Front content cannot be null or empty");
        }

        @Test
        @DisplayName("Given null back content, When updating content, Then should throw IllegalArgumentException")
        void givenNullBackContent_whenUpdatingContent_thenShouldThrowException() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);

            // When & Then
            assertThatThrownBy(() -> flashcard.updateContent(VALID_FRONT_CONTENT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Back content cannot be null or empty");
        }

        @Test
        @DisplayName("Given content exceeding 1000 characters, When updating content, Then should throw IllegalArgumentException")
        void givenTooLongContent_whenUpdatingContent_thenShouldThrowException() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);
            String longContent = "A".repeat(1001);

            // When & Then
            assertThatThrownBy(() -> flashcard.updateContent(longContent, VALID_BACK_CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Front content cannot exceed 1000 characters");
        }
    }

    @Nested
    @DisplayName("ensureOwnedBy() business method")
    class EnsureOwnedByMethod {

        @Test
        @DisplayName("Given flashcard owned by user, When ensuring ownership, Then should not throw exception")
        void givenFlashcardOwnedByUser_whenEnsuringOwnership_thenShouldNotThrow() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);

            // When & Then
            assertThatCode(() -> flashcard.ensureOwnedBy(TEST_USER_ID))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Given flashcard owned by different user, When ensuring ownership, Then should throw IllegalArgumentException")
        void givenFlashcardOwnedByDifferentUser_whenEnsuringOwnership_thenShouldThrowException() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);
            UUID otherUserId = UUID.randomUUID();

            // When & Then
            assertThatThrownBy(() -> flashcard.ensureOwnedBy(otherUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not owned by user");
        }
    }

    @Nested
    @DisplayName("isOwnedBy() business query")
    class IsOwnedByMethod {

        @Test
        @DisplayName("Given flashcard owned by user, When checking ownership, Then should return true")
        void givenFlashcardOwnedByUser_whenCheckingOwnership_thenShouldReturnTrue() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);

            // When
            boolean isOwned = flashcard.isOwnedBy(TEST_USER_ID);

            // Then
            assertThat(isOwned).isTrue();
        }

        @Test
        @DisplayName("Given flashcard owned by different user, When checking ownership, Then should return false")
        void givenFlashcardOwnedByDifferentUser_whenCheckingOwnership_thenShouldReturnFalse() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);
            UUID otherUserId = UUID.randomUUID();

            // When
            boolean isOwned = flashcard.isOwnedBy(otherUserId);

            // Then
            assertThat(isOwned).isFalse();
        }
    }

    @Nested
    @DisplayName("isAiGenerated() business query")
    class IsAiGeneratedMethod {

        @Test
        @DisplayName("Given flashcard with AI source, When checking if AI generated, Then should return true")
        void givenFlashcardWithAiSource_whenCheckingAiGenerated_thenShouldReturnTrue() {
            // Given
            Flashcard flashcard = Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.AI, TEST_SESSION_ID
            );

            // When
            boolean isAiGenerated = flashcard.isAiGenerated();

            // Then
            assertThat(isAiGenerated).isTrue();
        }

        @Test
        @DisplayName("Given flashcard with AI_USER source, When checking if AI generated, Then should return true")
        void givenFlashcardWithAiUserSource_whenCheckingAiGenerated_thenShouldReturnTrue() {
            // Given
            Flashcard flashcard = Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.AI_USER, TEST_SESSION_ID
            );

            // When
            boolean isAiGenerated = flashcard.isAiGenerated();

            // Then
            assertThat(isAiGenerated).isTrue();
        }

        @Test
        @DisplayName("Given flashcard with USER source, When checking if AI generated, Then should return false")
        void givenFlashcardWithUserSource_whenCheckingAiGenerated_thenShouldReturnFalse() {
            // Given
            Flashcard flashcard = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);

            // When
            boolean isAiGenerated = flashcard.isAiGenerated();

            // Then
            assertThat(isAiGenerated).isFalse();
        }
    }

    @Nested
    @DisplayName("toSnapshot() snapshot pattern")
    class ToSnapshotMethod {

        @Test
        @DisplayName("Given flashcard, When converting to snapshot, Then should capture all state")
        void givenFlashcard_whenConvertingToSnapshot_thenShouldCaptureAllState() {
            // Given
            Flashcard flashcard = Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.AI, TEST_SESSION_ID
            );

            // When
            FlashcardSnapshot snapshot = flashcard.toSnapshot();

            // Then
            assertThat(snapshot.id()).isNotNull();
            assertThat(snapshot.userId()).isEqualTo(TEST_USER_ID);
            assertThat(snapshot.frontContent()).isEqualTo(VALID_FRONT_CONTENT);
            assertThat(snapshot.backContent()).isEqualTo(VALID_BACK_CONTENT);
            assertThat(snapshot.source()).isEqualTo(FlashcardSource.AI);
            assertThat(snapshot.generationSessionId()).isEqualTo(TEST_SESSION_ID);
            assertThat(snapshot.createdAt()).isNotNull();
            assertThat(snapshot.updatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("fromSnapshot() factory method")
    class FromSnapshotMethod {

        @Test
        @DisplayName("Given valid snapshot, When reconstructing flashcard, Then should restore all state")
        void givenValidSnapshot_whenReconstructingFlashcard_thenShouldRestoreAllState() {
            // Given
            Flashcard original = Flashcard.createFromAiSuggestion(
                TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT, FlashcardSource.AI, TEST_SESSION_ID
            );
            FlashcardSnapshot snapshot = original.toSnapshot();

            // When
            Flashcard reconstructed = Flashcard.fromSnapshot(snapshot);

            // Then
            FlashcardSnapshot reconstructedSnapshot = reconstructed.toSnapshot();
            assertThat(reconstructedSnapshot.id()).isEqualTo(snapshot.id());
            assertThat(reconstructedSnapshot.userId()).isEqualTo(snapshot.userId());
            assertThat(reconstructedSnapshot.frontContent()).isEqualTo(snapshot.frontContent());
            assertThat(reconstructedSnapshot.backContent()).isEqualTo(snapshot.backContent());
            assertThat(reconstructedSnapshot.source()).isEqualTo(snapshot.source());
            assertThat(reconstructedSnapshot.generationSessionId()).isEqualTo(snapshot.generationSessionId());
            assertThat(reconstructedSnapshot.createdAt()).isEqualTo(snapshot.createdAt());
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCodeMethod {

        @Test
        @DisplayName("Given two flashcards with same ID, When comparing, Then should be equal")
        void givenTwoFlashcardsWithSameId_whenComparing_thenShouldBeEqual() {
            // Given
            Flashcard flashcard1 = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);
            FlashcardSnapshot snapshot1 = flashcard1.toSnapshot();

            // Create flashcard2 from snapshot with same ID but different content
            FlashcardSnapshot snapshot2 = FlashcardSnapshot.builder()
                .id(snapshot1.id())  // Same ID
                .userId(UUID.randomUUID())  // Different user
                .frontContent("Different front")
                .backContent("Different back")
                .source(FlashcardSource.AI)
                .generationSessionId(TEST_SESSION_ID)
                .createdAt(snapshot1.createdAt())
                .updatedAt(snapshot1.updatedAt())
                .build();
            Flashcard flashcard2 = Flashcard.fromSnapshot(snapshot2);

            // When & Then
            assertThat(flashcard1).isEqualTo(flashcard2);
            assertThat(flashcard1.hashCode()).isEqualTo(flashcard2.hashCode());
        }

        @Test
        @DisplayName("Given two flashcards with different IDs, When comparing, Then should not be equal")
        void givenTwoFlashcardsWithDifferentIds_whenComparing_thenShouldNotBeEqual() {
            // Given
            Flashcard flashcard1 = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);
            Flashcard flashcard2 = Flashcard.createManual(TEST_USER_ID, VALID_FRONT_CONTENT, VALID_BACK_CONTENT);

            // When & Then
            assertThat(flashcard1).isNotEqualTo(flashcard2);
        }
    }
}
