package com.ten.devs.cards.cards.flashcards.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing an AI flashcard generation session.
 *
 * MUTABLE - state changes through business methods.
 * Pure domain logic - no infrastructure dependencies (no JPA annotations).
 *
 * Aggregate root - contains FlashcardSuggestion value objects.
 * Suggestions are part of this aggregate and have no independent lifecycle.
 *
 * Lifecycle patterns:
 * 1. State transitions: create() → complete() or fail()
 * 2. Direct creation: createCompleted() or createFailed() with pre-generated ID
 *
 * Factory methods:
 * - create() - creates PENDING session (when state transitions needed)
 * - createCompleted() - creates COMPLETED session with suggestions (preferred)
 * - createFailed() - creates FAILED session
 * - fromSnapshot() - reconstructs from database
 */
public class AiGenerationSession {

    private final AiGenerationSessionId id;
    private final UUID userId;
    private final String inputText;
    private final Instant createdAt;

    // Mutable state
    private List<FlashcardSuggestion> suggestions;
    private int generatedCount;
    private int acceptedCount;
    private String aiModel;
    private BigDecimal apiCost;
    private AiGenerationSessionStatus status;

    // Private constructor - use factory methods
    private AiGenerationSession(
            AiGenerationSessionId id,
            UUID userId,
            String inputText,
            List<FlashcardSuggestion> suggestions,
            int generatedCount,
            int acceptedCount,
            String aiModel,
            BigDecimal apiCost,
            AiGenerationSessionStatus status,
            Instant createdAt) {

        this.id = id;
        this.userId = userId;
        this.inputText = inputText;
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
        this.generatedCount = generatedCount;
        this.acceptedCount = acceptedCount;
        this.aiModel = aiModel;
        this.apiCost = apiCost;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Factory method - creates new PENDING session.
     *
     * @param userId user creating the session
     * @param inputText input text for AI generation
     * @return new session with PENDING status and empty suggestions
     */
    public static AiGenerationSession create(UUID userId, String inputText) {
        validateInputText(inputText);

        return new AiGenerationSession(
            AiGenerationSessionId.generate(),
            userId,
            inputText,
            new ArrayList<>(),  // Empty suggestions for PENDING session
            0,
            0,
            null,
            null,
            AiGenerationSessionStatus.PENDING,
            Instant.now()
        );
    }

    /**
     * Factory method - creates COMPLETED session with specific ID and suggestions.
     * Used when AI generation is successful and we want to create the domain object
     * with its final state.
     *
     * @param sessionId pre-generated session identifier
     * @param userId user creating the session
     * @param inputText input text for AI generation
     * @param suggestions AI-generated flashcard suggestions
     * @param aiModel AI model used for generation
     * @param apiCost cost of the API call
     * @return new session with COMPLETED status and suggestions
     */
    public static AiGenerationSession createCompleted(
            UUID sessionId,
            UUID userId,
            String inputText,
            List<FlashcardSuggestion> suggestions,
            String aiModel,
            BigDecimal apiCost) {

        validateInputText(inputText);

        if (suggestions == null || suggestions.isEmpty()) {
            throw new IllegalArgumentException("Cannot create completed session without suggestions");
        }

        return new AiGenerationSession(
            AiGenerationSessionId.of(sessionId),
            userId,
            inputText,
            suggestions,
            suggestions.size(),
            0,  // No accepted count yet
            aiModel,
            apiCost,
            AiGenerationSessionStatus.COMPLETED,
            Instant.now()
        );
    }

    /**
     * Factory method - creates FAILED session with specific ID.
     * Used when AI generation fails and we want to create the domain object
     * with FAILED state.
     *
     * @param sessionId pre-generated session identifier
     * @param userId user creating the session
     * @param inputText input text for AI generation
     * @return new session with FAILED status
     */
    public static AiGenerationSession createFailed(
            UUID sessionId,
            UUID userId,
            String inputText) {

        validateInputText(inputText);

        return new AiGenerationSession(
            AiGenerationSessionId.of(sessionId),
            userId,
            inputText,
            new ArrayList<>(),  // No suggestions for failed session
            0,
            0,
            null,
            null,
            AiGenerationSessionStatus.FAILED,
            Instant.now()
        );
    }

    /**
     * Factory method - reconstructs session from snapshot.
     *
     * @param snapshot state snapshot from database
     * @return domain entity
     */
    public static AiGenerationSession fromSnapshot(AiGenerationSessionSnapshot snapshot) {
        return new AiGenerationSession(
            AiGenerationSessionId.of(snapshot.id()),
            snapshot.userId(),
            snapshot.inputText(),
            snapshot.suggestions() != null ? snapshot.suggestions() : List.of(),
            snapshot.generatedCount() != null ? snapshot.generatedCount() : 0,
            snapshot.acceptedCount() != null ? snapshot.acceptedCount() : 0,
            snapshot.aiModel(),
            snapshot.apiCost(),
            snapshot.status(),
            snapshot.createdAt()
        );
    }

    /**
     * Business method - marks session as completed with suggestions.
     * Modifies the entity state.
     *
     * @param suggestions list of AI-generated flashcard suggestions
     * @param aiModel AI model used
     * @param apiCost cost of API call
     */
    public void complete(List<FlashcardSuggestion> suggestions, String aiModel, BigDecimal apiCost) {
        if (status != AiGenerationSessionStatus.PENDING) {
            throw new IllegalStateException(
                "Can only complete PENDING sessions. Current status: " + status);
        }
        if (suggestions == null || suggestions.isEmpty()) {
            throw new IllegalArgumentException("Cannot complete session without suggestions");
        }

        this.suggestions = new ArrayList<>(suggestions);  // Defensive copy
        this.generatedCount = suggestions.size();
        this.aiModel = aiModel;
        this.apiCost = apiCost;
        this.status = AiGenerationSessionStatus.COMPLETED;
    }

    /**
     * Business method - marks session as failed.
     * Modifies the entity state.
     */
    public void fail() {
        if (status != AiGenerationSessionStatus.PENDING) {
            throw new IllegalStateException(
                "Can only fail PENDING sessions. Current status: " + status);
        }

        this.status = AiGenerationSessionStatus.FAILED;
    }

    /**
     * Business method - updates accepted count when user accepts suggestions.
     * Modifies the entity state.
     *
     * @param acceptedCount number of accepted suggestions
     */
    public void updateAcceptedCount(int acceptedCount) {
        if (status != AiGenerationSessionStatus.COMPLETED) {
            throw new IllegalStateException(
                "Can only update accepted count for COMPLETED sessions. Current status: " + status);
        }
        if (acceptedCount < 0 || acceptedCount > generatedCount) {
            throw new IllegalArgumentException(
                "Accepted count must be between 0 and " + generatedCount);
        }

        this.acceptedCount = acceptedCount;
    }

    /**
     * Business method - verifies that the session is owned by the specified user.
     * Throws exception if not owned.
     *
     * @param userId user ID to check ownership against
     * @throws IllegalArgumentException if session is not owned by the user
     */
    public void ensureOwnedBy(UUID userId) {
        if (!this.userId.equals(userId)) {
            throw new IllegalArgumentException(
                "Session " + id + " is not owned by user " + userId);
        }
    }

    /**
     * Business query - checks if session is owned by the specified user.
     *
     * @param userId user ID to check
     * @return true if owned by user
     */
    public boolean isOwnedBy(UUID userId) {
        return this.userId.equals(userId);
    }

    /**
     * Business query - checks if session can provide suggestions.
     * Suggestions are only available for COMPLETED sessions.
     *
     * @return true if session is COMPLETED
     */
    public boolean canProvideSuggestions() {
        return status == AiGenerationSessionStatus.COMPLETED;
    }

    /**
     * Business query - checks if session is pending.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status == AiGenerationSessionStatus.PENDING;
    }

    /**
     * Business query - checks if session has failed.
     *
     * @return true if status is FAILED
     */
    public boolean hasFailed() {
        return status == AiGenerationSessionStatus.FAILED;
    }

    /**
     * Business query - checks if session is completed.
     *
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == AiGenerationSessionStatus.COMPLETED;
    }

    /**
     * Business query - returns immutable copy of suggestions.
     * This method provides controlled access to suggestions without exposing internal state.
     *
     * @return unmodifiable list of suggestions
     */
    public List<FlashcardSuggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    /**
     * Converts domain entity to snapshot for persistence.
     * This is the ONLY way to access entity state externally.
     *
     * @return snapshot with current state
     */
    public AiGenerationSessionSnapshot toSnapshot() {
        return AiGenerationSessionSnapshot.builder()
            .id(id.value())
            .userId(userId)
            .inputText(inputText)
            .suggestions(List.copyOf(suggestions))  // Defensive copy for snapshot
            .generatedCount(generatedCount)
            .acceptedCount(acceptedCount)
            .aiModel(aiModel)
            .apiCost(apiCost)
            .status(status)
            .createdAt(createdAt)
            .build();
    }

    // Validation helper
    private static void validateInputText(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }
        int length = inputText.length();
        if (length < 1000 || length > 10000) {
            throw new IllegalArgumentException(
                "Input text must be between 1000 and 10000 characters. Current: " + length);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiGenerationSession that = (AiGenerationSession) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AiGenerationSession{" +
            "id=" + id +
            ", userId=" + userId +
            ", status=" + status +
            ", generatedCount=" + generatedCount +
            ", acceptedCount=" + acceptedCount +
            '}';
    }
}