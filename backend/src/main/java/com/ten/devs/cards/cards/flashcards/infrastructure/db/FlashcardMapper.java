package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import com.ten.devs.cards.cards.flashcards.domain.Flashcard;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSnapshot;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between Flashcard domain and infrastructure layers.
 *
 * Handles conversion:
 * - Domain entity → JPA entity (via snapshot)
 * - JPA entity → Domain entity
 * - Domain entity → Snapshot
 *
 * Follows the same pattern as AiGenerationSessionMapper.
 */
@Mapper(componentModel = "spring")
public interface FlashcardMapper {

    /**
     * Converts domain snapshot to JPA entity.
     * Used when saving domain entity to database.
     *
     * @param snapshot domain snapshot
     * @return JPA entity
     */
    FlashcardEntity toEntity(FlashcardSnapshot snapshot);

    /**
     * Converts JPA entity to domain snapshot.
     * Used when loading entity from database.
     *
     * @param entity JPA entity
     * @return domain snapshot
     */
    FlashcardSnapshot toSnapshot(FlashcardEntity entity);

    /**
     * Converts JPA entity to domain entity.
     * Convenience method that combines toSnapshot and fromSnapshot.
     *
     * @param entity JPA entity
     * @return domain entity
     */
    default Flashcard toDomain(FlashcardEntity entity) {
        if (entity == null) {
            return null;
        }
        FlashcardSnapshot snapshot = toSnapshot(entity);
        return Flashcard.fromSnapshot(snapshot);
    }

    /**
     * Converts domain entity to JPA entity.
     * Convenience method that uses entity's toSnapshot.
     *
     * @param flashcard domain entity
     * @return JPA entity
     */
    default FlashcardEntity fromDomain(Flashcard flashcard) {
        if (flashcard == null) {
            return null;
        }
        return toEntity(flashcard.toSnapshot());
    }
}