package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.dto.OpenRouterRequest;
import com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.dto.OpenRouterResponse;
import com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class OpenRouterApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final OpenRouterConfiguration config;

    public OpenRouterApiClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            OpenRouterConfiguration config) {

        this.objectMapper = objectMapper;
        this.config = config;
        this.webClient = webClientBuilder
            .baseUrl(config.getBaseUrl())
            .defaultHeader("Authorization", "Bearer " + config.getApiKey())
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("HTTP-Referer", config.getAppUrl())
            .defaultHeader("X-Title", config.getAppName())
            .build();
    }

    public OpenRouterResponse sendRequest(OpenRouterRequest request) {
        log.info("Sending OpenRouter request - model: {}", request.getModel());

        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
            .bodyToMono(OpenRouterResponse.class)
            .retryWhen(buildRetrySpec())
            .block();
    }

    private Retry buildRetrySpec() {
        return Retry.backoff(
                config.getRetry().getMaxAttempts(),
                Duration.ofMillis(config.getRetry().getInitialBackoff()))
            .maxBackoff(Duration.ofMillis(config.getRetry().getMaxBackoff()))
            .filter(this::isRetryableError)
            .doBeforeRetry(signal ->
                log.warn("Retrying OpenRouter request, attempt: {}", signal.totalRetries() + 1));
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof ServerErrorException) {
            return true;
        }

        if (throwable instanceof RateLimitException) {
            return true;
        }

        return throwable instanceof IOException ||
               throwable instanceof TimeoutException;
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
            .flatMap(body -> {
                int status = response.statusCode().value();
                log.error("OpenRouter API error - status: {}, body: {}", status, body);

                return switch (status) {
                    case 401, 403 -> Mono.error(
                        new AuthenticationException("Invalid API key", body));

                    case 429 -> {
                        String retryAfter = response.headers()
                            .header("Retry-After")
                            .stream()
                            .findFirst()
                            .orElse("60");
                        yield Mono.error(
                            new RateLimitException("Rate limit exceeded",
                                Integer.parseInt(retryAfter)));
                    }

                    case 400, 422 -> Mono.error(
                        new InvalidRequestException("Invalid request", body));

                    case 500, 502, 503, 504 -> Mono.error(
                        new ServerErrorException("Server error", status, body));

                    default -> Mono.error(
                        new OpenRouterException("Unexpected error", status, body));
                };
            });
    }
}