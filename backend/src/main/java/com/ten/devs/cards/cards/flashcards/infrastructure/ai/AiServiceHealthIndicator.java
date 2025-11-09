package com.ten.devs.cards.cards.flashcards.infrastructure.ai;

import com.ten.devs.cards.cards.flashcards.application.service.AiServiceApi;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for AI service integration.
 * Monitors the connection status to the AI service provider.
 *
 * IMPORTANT: Depends on GENERIC AiServiceApi interface, not concrete implementation.
 * This allows monitoring any AI service implementation (OpenRouter, Anthropic, etc.).
 */
@Component("aiService")
@RequiredArgsConstructor
public class AiServiceHealthIndicator implements HealthIndicator {

    // Dependency on GENERIC INTERFACE (Port), not implementation
    private final AiServiceApi aiService;

    @Override
    public Health health() {
        try {
            boolean connected = aiService.testConnection();

            if (connected) {
                return Health.up()
                    .withDetail("status", "Connected")
                    .withDetail("service", "AI Flashcard Generation Service")
                    .build();
            } else {
                return Health.down()
                    .withDetail("status", "Connection failed")
                    .withDetail("reason", "Test connection returned false")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("status", "Error")
                .withDetail("error", e.getMessage())
                .withDetail("errorType", e.getClass().getSimpleName())
                .build();
        }
    }
}