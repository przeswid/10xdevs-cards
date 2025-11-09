package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import com.ten.devs.cards.cards.flashcards.domain.FlashcardSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for flashcards table.
 *
 * MUTABLE - required by JPA/Hibernate.
 * Infrastructure concern - contains JPA annotations.
 *
 * Maps to database table, separate from domain entity.
 * Conversion handled by FlashcardMapper.
 */
@Entity
@Table(name = "flashcards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "front_content", nullable = false, length = 1000)
    private String frontContent;

    @Column(name = "back_content", nullable = false, length = 1000)
    private String backContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private FlashcardSource source;

    @Column(name = "generation_session_id")
    private UUID generationSessionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}