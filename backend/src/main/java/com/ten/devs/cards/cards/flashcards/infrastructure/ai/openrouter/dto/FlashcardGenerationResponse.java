package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response parsed from model content (JSON Schema response).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardGenerationResponse {

    private List<FlashcardDto> flashcards;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashcardDto {
        private String front;
        private String back;
    }
}