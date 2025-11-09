package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception;

import lombok.Getter;

/**
 * Main exception for OpenRouter errors.
 */
@Getter
public class OpenRouterException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public OpenRouterException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public OpenRouterException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }
}