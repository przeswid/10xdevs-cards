package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import com.ten.devs.cards.cards.flashcards.domain.Flashcard;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL database implementation of FlashcardRepository.
 *
 * ADAPTER in hexagonal architecture - implements domain PORT.
 * Bridges domain layer (Flashcard entity) and infrastructure (JPA).
 *
 * Uses:
 * - FlashcardJpaRepository for database operations
 * - FlashcardMapper for domain ↔ entity conversion
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SqlDbFlashcardRepository implements FlashcardRepository {

    private final FlashcardJpaRepository jpaRepository;
    private final FlashcardMapper mapper;

    @Override
    public Flashcard save(Flashcard flashcard) {
        log.debug("Saving flashcard: {}", flashcard.toSnapshot().id());

        FlashcardEntity entity = mapper.fromDomain(flashcard);
        FlashcardEntity savedEntity = jpaRepository.save(entity);

        log.debug("Flashcard saved: {}", savedEntity.getId());
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Flashcard> saveAll(List<Flashcard> flashcards) {
        log.debug("Saving {} flashcards", flashcards.size());

        List<FlashcardEntity> entities = flashcards.stream()
            .map(mapper::fromDomain)
            .toList();
        List<FlashcardEntity> savedEntities = jpaRepository.saveAll(entities);

        log.debug("Saved {} flashcards", savedEntities.size());
        return savedEntities.stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Flashcard> findById(UUID id) {
        log.debug("Finding flashcard by ID: {}", id);

        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<Flashcard> findByUserId(UUID userId) {
        log.debug("Finding flashcards for user: {}", userId);

        List<FlashcardEntity> entities = jpaRepository.findByUserId(userId);

        log.debug("Found {} flashcards for user {}", entities.size(), userId);
        return entities.stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Flashcard> findByGenerationSessionId(UUID sessionId) {
        log.debug("Finding flashcards for session: {}", sessionId);

        List<FlashcardEntity> entities = jpaRepository.findByGenerationSessionId(sessionId);

        log.debug("Found {} flashcards for session {}", entities.size(), sessionId);
        return entities.stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting flashcard: {}", id);
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}