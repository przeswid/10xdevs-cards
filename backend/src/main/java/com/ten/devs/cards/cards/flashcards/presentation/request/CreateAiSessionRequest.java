package com.ten.devs.cards.cards.flashcards.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating AI generation session
 * Maps to POST /ai/sessions endpoint
 * Based on ai_generation_sessions table input_text field
 */
public record CreateAiSessionRequest(
    @NotBlank(message = "Input text is required")
    @Size(min = 1000, max = 10000, message = "Input text must be between 1000 and 10000 characters")
    String inputText
) {
}