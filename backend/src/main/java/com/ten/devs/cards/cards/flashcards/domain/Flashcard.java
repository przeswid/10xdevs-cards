package com.ten.devs.cards.cards.flashcards.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a user's flashcard.
 *
 * MUTABLE - state changes through business methods.
 * Pure domain logic - no infrastructure dependencies (no JPA annotations).
 *
 * Lifecycle:
 * - Created from AI suggestions (source: AI or AI_USER)
 * - Created manually by user (source: USER)
 * - Content can be updated
 *
 * Factory methods:
 * - createFromAiSuggestion() - creates flashcard from approved AI suggestion
 * - createManual() - creates user-generated flashcard
 */
public class Flashcard {

    private final UUID id;
    private final UUID userId;
    private final UUID generationSessionId;  // Optional - only for AI-generated cards
    private final FlashcardSource source;
    private final Instant createdAt;

    // Mutable state
    private String frontContent;
    private String backContent;
    private Instant updatedAt;

    // Private constructor - use factory methods
    private Flashcard(
            UUID id,
            UUID userId,
            String frontContent,
            String backContent,
            FlashcardSource source,
            UUID generationSessionId,
            Instant createdAt,
            Instant updatedAt) {

        this.id = id;
        this.userId = userId;
        this.frontContent = frontContent;
        this.backContent = backContent;
        this.source = source;
        this.generationSessionId = generationSessionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method - creates flashcard from approved AI suggestion.
     *
     * @param userId user creating the flashcard
     * @param frontContent front side content
     * @param backContent back side content
     * @param source flashcard source (AI or AI_USER)
     * @param generationSessionId AI generation session ID
     * @return new flashcard from AI suggestion
     */
    public static Flashcard createFromAiSuggestion(
            UUID userId,
            String frontContent,
            String backContent,
            FlashcardSource source,
            UUID generationSessionId) {

        validateContent(frontContent, backContent);

        if (source != FlashcardSource.AI && source != FlashcardSource.AI_USER) {
            throw new IllegalArgumentException(
                "AI-generated flashcards must have source AI or AI_USER, got: " + source);
        }

        if (generationSessionId == null) {
            throw new IllegalArgumentException(
                "AI-generated flashcards must have generation session ID");
        }

        Instant now = Instant.now();

        return new Flashcard(
            UUID.randomUUID(),
            userId,
            frontContent,
            backContent,
            source,
            generationSessionId,
            now,
            now
        );
    }

    /**
     * Factory method - creates manually created flashcard.
     *
     * @param userId user creating the flashcard
     * @param frontContent front side content
     * @param backContent back side content
     * @return new user-created flashcard
     */
    public static Flashcard createManual(
            UUID userId,
            String frontContent,
            String backContent) {

        validateContent(frontContent, backContent);

        Instant now = Instant.now();

        return new Flashcard(
            UUID.randomUUID(),
            userId,
            frontContent,
            backContent,
            FlashcardSource.USER,
            null,  // No generation session for manual cards
            now,
            now
        );
    }

    /**
     * Factory method - reconstructs flashcard from snapshot.
     * Preserves all original values including ID and timestamps.
     *
     * @param snapshot state snapshot from database
     * @return domain entity
     */
    public static Flashcard fromSnapshot(FlashcardSnapshot snapshot) {
        return new Flashcard(
            snapshot.id(),
            snapshot.userId(),
            snapshot.frontContent(),
            snapshot.backContent(),
            snapshot.source(),
            snapshot.generationSessionId(),
            snapshot.createdAt(),
            snapshot.updatedAt()
        );
    }

    /**
     * Business method - updates flashcard content.
     *
     * @param frontContent new front content
     * @param backContent new back content
     */
    public void updateContent(String frontContent, String backContent) {
        validateContent(frontContent, backContent);

        this.frontContent = frontContent;
        this.backContent = backContent;
        this.updatedAt = Instant.now();
    }

    /**
     * Business method - verifies that the flashcard is owned by the specified user.
     *
     * @param userId user ID to check ownership against
     * @throws IllegalArgumentException if flashcard is not owned by the user
     */
    public void ensureOwnedBy(UUID userId) {
        if (!this.userId.equals(userId)) {
            throw new IllegalArgumentException(
                "Flashcard " + id + " is not owned by user " + userId);
        }
    }

    /**
     * Business query - checks if flashcard is owned by the specified user.
     *
     * @param userId user ID to check
     * @return true if owned by user
     */
    public boolean isOwnedBy(UUID userId) {
        return this.userId.equals(userId);
    }

    /**
     * Business query - checks if flashcard was AI-generated.
     *
     * @return true if source is AI or AI_USER
     */
    public boolean isAiGenerated() {
        return source == FlashcardSource.AI || source == FlashcardSource.AI_USER;
    }

    /**
     * Converts domain entity to snapshot for persistence.
     * This is the ONLY way to access entity state externally.
     *
     * @return snapshot with current state
     */
    public FlashcardSnapshot toSnapshot() {
        return FlashcardSnapshot.builder()
            .id(id)
            .userId(userId)
            .frontContent(frontContent)
            .backContent(backContent)
            .source(source)
            .generationSessionId(generationSessionId)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    // Validation helper
    private static void validateContent(String frontContent, String backContent) {
        if (frontContent == null || frontContent.isBlank()) {
            throw new IllegalArgumentException("Front content cannot be null or empty");
        }
        if (backContent == null || backContent.isBlank()) {
            throw new IllegalArgumentException("Back content cannot be null or empty");
        }
        if (frontContent.length() > 1000) {
            throw new IllegalArgumentException(
                "Front content cannot exceed 1000 characters. Current: " + frontContent.length());
        }
        if (backContent.length() > 1000) {
            throw new IllegalArgumentException(
                "Back content cannot exceed 1000 characters. Current: " + backContent.length());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return id.equals(flashcard.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Flashcard{" +
            "id=" + id +
            ", userId=" + userId +
            ", source=" + source +
            ", generationSessionId=" + generationSessionId +
            '}';
    }
}