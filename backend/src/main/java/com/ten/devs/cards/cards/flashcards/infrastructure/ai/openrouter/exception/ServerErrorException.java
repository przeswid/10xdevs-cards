package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception;

/**
 * OpenRouter server error (500, 502, 503, 504).
 * RETRY with exponential backoff.
 */
public class ServerErrorException extends OpenRouterException {
    public ServerErrorException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
}