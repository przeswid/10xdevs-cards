package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception;

/**
 * Authentication error (401, 403).
 * DO NOT RETRY - requires configuration fix.
 */
public class AuthenticationException extends OpenRouterException {
    public AuthenticationException(String message, String responseBody) {
        super(message, 401, responseBody);
    }
}