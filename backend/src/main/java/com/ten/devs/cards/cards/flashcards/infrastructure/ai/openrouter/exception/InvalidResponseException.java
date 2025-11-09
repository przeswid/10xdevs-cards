package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception;

/**
 * Invalid response (parsing error).
 */
public class InvalidResponseException extends RuntimeException {
    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}