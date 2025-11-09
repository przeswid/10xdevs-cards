package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "openrouter")
@Validated
@Data
public class OpenRouterConfiguration {

    @NotBlank(message = "OpenRouter API key must be configured")
    private String apiKey;

    @NotBlank
    private String baseUrl = "https://openrouter.ai/api/v1";

    @NotBlank
    private String appUrl;

    @NotBlank
    private String appName;

    @NotBlank
    private String defaultModel = "openai/gpt-4o-mini";

    private ModelParametersConfig defaultParameters = new ModelParametersConfig();
    private RetryConfig retry = new RetryConfig();
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    @Data
    public static class ModelParametersConfig {
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
        private Double topP = 0.9;
        private Double frequencyPenalty = 0.3;
        private Double presencePenalty = 0.1;
    }

    @Data
    public static class RetryConfig {
        private Integer maxAttempts = 3;
        private Long initialBackoff = 1000L;
        private Long maxBackoff = 10000L;
        private Double multiplier = 2.0;
    }

    @Data
    public static class CircuitBreakerConfig {
        private Integer slidingWindowSize = 10;
        private Float failureRateThreshold = 50.0f;
        private Long waitDurationInOpenState = 60000L;
        private Integer permittedCallsInHalfOpenState = 3;
    }
}