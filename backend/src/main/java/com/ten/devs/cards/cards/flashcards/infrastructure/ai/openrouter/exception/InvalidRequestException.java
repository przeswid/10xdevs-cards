package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception;

/**
 * Invalid request (400, 422).
 * DO NOT RETRY - requires input data fix.
 */
public class InvalidRequestException extends OpenRouterException {
    public InvalidRequestException(String message, String responseBody) {
        super(message, 400, responseBody);
    }
}