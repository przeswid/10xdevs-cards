package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSession;
import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between domain and infrastructure layers.
 *
 * Handles conversion:
 * - Domain entity → JPA entity (via snapshot)
 * - JPA entity → Domain entity
 * - Domain entity → Snapshot
 *
 * Uses FlashcardSuggestionMapper for mapping suggestions collection.
 */
@Mapper(componentModel = "spring", uses = FlashcardSuggestionMapper.class)
public interface AiGenerationSessionMapper {

    /**
     * Converts domain snapshot to JPA entity.
     * Used when saving domain entity to database.
     *
     * @param snapshot domain snapshot
     * @return JPA entity
     */
    AiGenerationSessionEntity toEntity(AiGenerationSessionSnapshot snapshot);

    /**
     * Converts JPA entity to domain snapshot.
     * Used when loading entity from database.
     *
     * @param entity JPA entity
     * @return domain snapshot
     */
    AiGenerationSessionSnapshot toSnapshot(AiGenerationSessionEntity entity);

    /**
     * Converts JPA entity to domain entity.
     * Convenience method that combines toSnapshot and fromSnapshot.
     *
     * @param entity JPA entity
     * @return domain entity
     */
    default AiGenerationSession toDomain(AiGenerationSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        AiGenerationSessionSnapshot snapshot = toSnapshot(entity);
        return AiGenerationSession.fromSnapshot(snapshot);
    }

    /**
     * Converts domain entity to JPA entity.
     * Convenience method that uses entity's toSnapshot.
     *
     * @param domain domain entity
     * @return JPA entity
     */
    default AiGenerationSessionEntity fromDomain(AiGenerationSession domain) {
        if (domain == null) {
            return null;
        }
        return toEntity(domain.toSnapshot());
    }
}