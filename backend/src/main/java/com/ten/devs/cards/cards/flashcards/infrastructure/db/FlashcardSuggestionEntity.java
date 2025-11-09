package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for flashcard_suggestions table.
 *
 * MUTABLE - required by JPA/Hibernate.
 * Infrastructure concern - contains JPA annotations.
 *
 * Part of AiGenerationSession aggregate - managed via @OneToMany relationship.
 * The session_id is managed by the parent (AiGenerationSessionEntity) via @JoinColumn,
 * so this field is marked as insertable=false, updatable=false (read-only).
 */
@Entity
@Table(name = "flashcard_suggestions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSuggestionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Foreign key to ai_generation_sessions.
     * Managed by parent entity's @JoinColumn - this field is read-only.
     */
    @Column(name = "session_id", nullable = false, insertable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "front_content", nullable = false, length = 1000)
    private String frontContent;

    @Column(name = "back_content", nullable = false, length = 1000)
    private String backContent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}