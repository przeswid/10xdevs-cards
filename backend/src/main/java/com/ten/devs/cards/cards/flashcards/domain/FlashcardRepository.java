package com.ten.devs.cards.cards.flashcards.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Flashcard domain entity.
 *
 * PORT in hexagonal architecture - defines contract for persistence.
 * Implementation is in infrastructure layer (ADAPTER).
 *
 * Follows repository pattern:
 * - Domain layer defines interface (this file)
 * - Infrastructure layer implements it (SqlDbFlashcardRepository)
 * - Application layer depends on this interface, not implementation
 */
public interface FlashcardRepository {

    /**
     * Saves a flashcard (create or update).
     *
     * @param flashcard domain entity to save
     * @return saved flashcard with updated state
     */
    Flashcard save(Flashcard flashcard);

    /**
     * Saves multiple flashcards in batch.
     *
     * @param flashcards domain entities to save
     * @return saved flashcards
     */
    List<Flashcard> saveAll(List<Flashcard> flashcards);

    /**
     * Finds a flashcard by ID.
     *
     * @param id flashcard ID
     * @return optional containing flashcard if found, empty otherwise
     */
    Optional<Flashcard> findById(UUID id);

    /**
     * Finds all flashcards for a specific user.
     *
     * @param userId user ID
     * @return list of flashcards owned by the user
     */
    List<Flashcard> findByUserId(UUID userId);

    /**
     * Finds all flashcards created from a specific AI generation session.
     *
     * @param sessionId AI generation session ID
     * @return list of flashcards from that session
     */
    List<Flashcard> findByGenerationSessionId(UUID sessionId);

    /**
     * Deletes a flashcard by ID.
     *
     * @param id flashcard ID
     */
    void deleteById(UUID id);

    /**
     * Checks if a flashcard exists by ID.
     *
     * @param id flashcard ID
     * @return true if flashcard exists
     */
    boolean existsById(UUID id);
}