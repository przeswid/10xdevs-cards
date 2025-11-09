package com.ten.devs.cards.cards.flashcards.infrastructure.db;

import com.ten.devs.cards.cards.flashcards.domain.AiGenerationSessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for ai_generation_sessions table.
 *
 * MUTABLE - required by JPA/Hibernate.
 * Infrastructure concern - contains JPA annotations.
 *
 * Maps to database table, separate from domain entity.
 * Contains @OneToMany relationship with FlashcardSuggestionEntity.
 * Conversion handled by mapper.
 */
@Entity
@Table(name = "ai_generation_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationSessionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @Builder.Default
    private List<FlashcardSuggestionEntity> suggestions = new ArrayList<>();

    @Column(name = "generated_count", nullable = false)
    @Builder.Default
    private Integer generatedCount = 0;

    @Column(name = "accepted_count", nullable = false)
    @Builder.Default
    private Integer acceptedCount = 0;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "api_cost", precision = 10, scale = 4)
    private BigDecimal apiCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AiGenerationSessionStatus status = AiGenerationSessionStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}