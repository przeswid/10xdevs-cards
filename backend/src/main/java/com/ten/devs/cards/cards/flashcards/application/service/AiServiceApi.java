package com.ten.devs.cards.cards.flashcards.application.service;

import com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port (interface) for AI flashcard generation service.
 *
 * GENERIC interface - NOT related to concrete implementation (OpenRouter, Anthropic, etc.).
 * Defines business contract: "I need an AI service for generating flashcards".
 *
 * According to hexagonal architecture, command handlers depend on this interface,
 * not on concrete implementation (OpenRouter, Anthropic, etc.).
 */
public interface AiServiceApi {

    /**
     * Generates flashcards based on provided text.
     *
     * @param inputText input text (1000-10000 characters)
     * @param sessionId generation session identifier
     * @return list of generated flashcard suggestions
     */
    List<FlashcardSuggestion> generateFlashcards(
            String inputText,
            UUID sessionId);

    /**
     * Tests connection with AI service.
     * Used for health checks and initialization.
     *
     * @return true if connection works properly
     */
    boolean testConnection();

    /**
     * Estimates cost of generating flashcards for given text.
     *
     * @param inputText text for analysis
     * @return estimated cost in USD
     */
    BigDecimal estimateCost(String inputText);
}