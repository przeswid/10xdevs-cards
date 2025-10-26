package com.ten.devs.cards.cards.flashcards.presentation.request;

/**
 * Request DTO for creating AI generation session
 * Maps to POST /ai/sessions endpoint
 * Based on ai_generation_sessions table input_text field
 */
public record CreateAiSessionRequest(
    String inputText  // 1000-10000 characters as per database constraint
) {
}