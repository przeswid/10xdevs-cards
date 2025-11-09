package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for FlashcardEntity.
 *
 * Infrastructure adapter - provides JPA persistence implementation.
 * Extends JpaRepository for standard CRUD operations.
 *
 * Custom query methods follow Spring Data naming conventions.
 */
@Repository
public interface FlashcardJpaRepository extends JpaRepository<FlashcardEntity, UUID> {

    /**
     * Finds all flashcards for a specific user.
     *
     * @param userId user ID
     * @return list of flashcard entities
     */
    List<FlashcardEntity> findByUserId(UUID userId);

    /**
     * Finds all flashcards created from a specific AI generation session.
     *
     * @param generationSessionId AI generation session ID
     * @return list of flashcard entities
     */
    List<FlashcardEntity> findByGenerationSessionId(UUID generationSessionId);
}