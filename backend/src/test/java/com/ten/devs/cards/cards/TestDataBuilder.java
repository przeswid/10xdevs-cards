package com.ten.devs.cards.cards;

import java.util.UUID;

/**
 * Builder pattern for creating test data.
 *
 * This utility class provides methods to create test data with realistic values
 * for use in unit and integration tests.
 *
 * Usage:
 * <pre>
 * {@code
 * UUID userId = TestDataBuilder.randomUserId();
 * String email = TestDataBuilder.randomEmail();
 * }
 * </pre>
 */
public class TestDataBuilder {

    private TestDataBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a random UUID for test user IDs.
     */
    public static UUID randomUserId() {
        return UUID.randomUUID();
    }

    /**
     * Generates a random email address for testing.
     */
    public static String randomEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    /**
     * Generates a random username for testing.
     */
    public static String randomUsername() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a test password.
     */
    public static String testPassword() {
        return "TestPassword123!";
    }

    /**
     * Generates random input text for AI generation (1000-10000 chars).
     */
    public static String randomInputText(int length) {
        if (length < 1000 || length > 10000) {
            throw new IllegalArgumentException("Input text must be between 1000 and 10000 characters");
        }
        StringBuilder sb = new StringBuilder();
        String sample = "This is sample text for AI flashcard generation. ";
        while (sb.length() < length) {
            sb.append(sample);
        }
        return sb.substring(0, length);
    }

    /**
     * Generates minimum valid input text (1000 chars).
     */
    public static String minInputText() {
        return randomInputText(1000);
    }

    /**
     * Generates maximum valid input text (10000 chars).
     */
    public static String maxInputText() {
        return randomInputText(10000);
    }

    /**
     * Generates flashcard front content.
     */
    public static String flashcardFront() {
        return "What is Domain-Driven Design?";
    }

    /**
     * Generates flashcard back content.
     */
    public static String flashcardBack() {
        return "Domain-Driven Design (DDD) is an approach to software development that centers the development on programming a domain model that has a rich understanding of the processes and rules of a domain.";
    }
}
