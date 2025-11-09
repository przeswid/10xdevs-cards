package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception;

import lombok.Getter;

/**
 * Rate limit exceeded (429).
 * RETRY with delay according to Retry-After header.
 */
@Getter
public class RateLimitException extends OpenRouterException {
    private final int retryAfterSeconds;

    public RateLimitException(String message, int retryAfterSeconds) {
        super(message, 429, null);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}