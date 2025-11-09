package com.ten.devs.cards.cards.flashcards.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository PORT for AiGenerationSession domain entity.
 *
 * GENERIC interface - represents business need: "I need to persist sessions"
 * NOT related to implementation (PostgreSQL, MongoDB, etc.)
 *
 * Following hexagonal architecture, this interface is defined in the domain layer
 * and implemented in the infrastructure layer.
 */
public interface AiGenerationSessionRepository {

    /**
     * Saves (creates or updates) a session.
     *
     * @param session domain entity to save
     * @return saved session
     */
    AiGenerationSession save(AiGenerationSession session);

    /**
     * Finds session by its domain identifier.
     *
     * @param id session identifier (value object)
     * @return session if found
     */
    Optional<AiGenerationSession> findById(AiGenerationSessionId id);

    /**
     * Finds session by UUID.
     * Convenience method for cases where we have UUID directly.
     *
     * @param id session UUID
     * @return session if found
     */
    Optional<AiGenerationSession> findById(UUID id);

    /**
     * Deletes session by its identifier.
     *
     * @param id session identifier
     */
    void delete(AiGenerationSessionId id);

    /**
     * Checks if session exists.
     *
     * @param id session identifier
     * @return true if exists
     */
    boolean existsById(AiGenerationSessionId id);
}