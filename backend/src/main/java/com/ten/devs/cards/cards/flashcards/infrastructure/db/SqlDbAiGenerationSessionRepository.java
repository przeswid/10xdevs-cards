package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionId;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * ADAPTER (implementation) for AiGenerationSessionRepository PORT.
 *
 * CONCRETE class - name indicates PostgreSQL/SQL implementation.
 * Implements GENERIC domain repository interface.
 *
 * Bridges domain layer (AiGenerationSession) and infrastructure layer (JPA).
 * Handles conversion between domain entities and JPA entities.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlDbAiGenerationSessionRepository implements AiGenerationSessionRepository {

    private final AiGenerationSessionJpaRepository jpaRepository;
    private final AiGenerationSessionMapper mapper;

    @Override
    public AiGenerationSession save(AiGenerationSession session) {
        log.debug("Saving AI generation session: {}", session.toSnapshot().id());

        // Convert domain → JPA entity
        AiGenerationSessionEntity entity = mapper.fromDomain(session);

        // Persist via Spring Data JPA
        AiGenerationSessionEntity saved = jpaRepository.save(entity);

        // Convert JPA entity → domain
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AiGenerationSession> findById(AiGenerationSessionId id) {
        return findById(id.value());
    }

    @Override
    public Optional<AiGenerationSession> findById(UUID id) {
        log.debug("Finding AI generation session by ID: {}", id);

        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public void delete(AiGenerationSessionId id) {
        log.debug("Deleting AI generation session: {}", id);
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(AiGenerationSessionId id) {
        return jpaRepository.existsById(id.value());
    }
}