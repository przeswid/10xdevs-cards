package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for AiGenerationSessionEntity.
 *
 * Infrastructure component - provides persistence operations for JPA entities.
 * Separate from domain repository interface.
 */
@Repository
public interface AiGenerationSessionJpaRepository extends JpaRepository<AiGenerationSessionEntity, UUID> {

    // Spring Data JPA automatically provides:
    // - save(entity)
    // - findById(id)
    // - existsById(id)
    // - delete(entity)
    // - deleteById(id)

    // No custom queries needed for MVP
}