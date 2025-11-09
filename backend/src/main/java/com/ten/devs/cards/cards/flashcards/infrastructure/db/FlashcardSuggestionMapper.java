package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper for converting FlashcardSuggestion between domain and infrastructure layers.
 *
 * Handles conversion:
 * - Domain entity → JPA entity
 * - JPA entity → Domain entity
 */
@Mapper(componentModel = "spring")
public interface FlashcardSuggestionMapper {

    /**
     * Converts domain entity to JPA entity.
     *
     * @param suggestion domain entity
     * @return JPA entity
     */
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    FlashcardSuggestionEntity toEntity(FlashcardSuggestion suggestion);

    /**
     * Converts JPA entity to domain entity.
     * Includes the ID from the database.
     *
     * @param entity JPA entity
     * @return domain entity with ID populated
     */
    @Mapping(target = "id", source = "id")
    FlashcardSuggestion toDomain(FlashcardSuggestionEntity entity);

    /**
     * Converts list of domain entities to JPA entities.
     *
     * @param suggestions domain entities
     * @return JPA entities
     */
    List<FlashcardSuggestionEntity> toEntities(List<FlashcardSuggestion> suggestions);

    /**
     * Converts list of JPA entities to domain entities.
     *
     * @param entities JPA entities
     * @return domain entities
     */
    List<FlashcardSuggestion> toDomainList(List<FlashcardSuggestionEntity> entities);
}