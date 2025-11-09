package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ten.devs.cards.cards.flashcards.application.service.AiServiceApi;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSuggestion;
import com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.dto.*;
import com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.exception.*;
import com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.prompt.FlashcardPromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component  // NOT @Service - we use @Component for infrastructure adapters
@RequiredArgsConstructor
public class OpenRouterService implements AiServiceApi {

    private final OpenRouterApiClient apiClient;
    private final FlashcardPromptBuilder promptBuilder;
    private final OpenRouterConfiguration config;
    private final ObjectMapper objectMapper;

    @Override
    public List<FlashcardSuggestion> generateFlashcards(
            String inputText,
            UUID sessionId) {

        log.info("Generating flashcards for session: {}", sessionId);

        validateInputText(inputText);

        OpenRouterRequest request = buildFlashcardGenerationRequest(inputText);

        OpenRouterResponse response = apiClient.sendRequest(request);
        return parseFlashcardResponse(response, sessionId);
    }

    @Override
    public boolean testConnection() {
        try {
            OpenRouterRequest testRequest = buildTestRequest();
            OpenRouterResponse response = apiClient.sendRequest(testRequest);
            return response != null && response.getId() != null;
        } catch (Exception e) {
            log.error("OpenRouter connection test failed", e);
            return false;
        }
    }

    @Override
    public BigDecimal estimateCost(String inputText) {
        int estimatedTokens = estimateTokenCount(inputText);
        String model = config.getDefaultModel();
        return calculateCost(model, estimatedTokens);
    }

    private void validateInputText(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new InvalidRequestException("Input text cannot be empty", null);
        }

        int length = inputText.length();
        if (length < 1000) {
            throw new InvalidRequestException(
                "Input text too short: " + length + " chars (minimum: 1000)", null);
        }

        if (length > 10000) {
            throw new InvalidRequestException(
                "Input text too long: " + length + " chars (maximum: 10000)", null);
        }
    }

    private OpenRouterRequest buildFlashcardGenerationRequest(String inputText) {
        List<Message> messages = List.of(
            Message.system(promptBuilder.buildSystemMessage()),
            Message.user(promptBuilder.buildUserMessage(inputText))
        );

        return OpenRouterRequest.builder()
            .model(config.getDefaultModel())
            .messages(messages)
            .responseFormat(promptBuilder.buildResponseFormat())
            .temperature(config.getDefaultParameters().getTemperature())
            .maxTokens(config.getDefaultParameters().getMaxTokens())
            .topP(config.getDefaultParameters().getTopP())
            .frequencyPenalty(config.getDefaultParameters().getFrequencyPenalty())
            .presencePenalty(config.getDefaultParameters().getPresencePenalty())
            .build();
    }

    private OpenRouterRequest buildTestRequest() {
        return OpenRouterRequest.builder()
            .model(config.getDefaultModel())
            .messages(List.of(Message.user("test")))
            .maxTokens(5)
            .build();
    }

    private List<FlashcardSuggestion> parseFlashcardResponse(
            OpenRouterResponse response,
            UUID sessionId) {

        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new InvalidResponseException("No choices in response");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        log.debug("Parsing flashcard response - content length: {}", content.length());

        try {
            FlashcardGenerationResponse parsed =
                objectMapper.readValue(content, FlashcardGenerationResponse.class);

            return parsed.getFlashcards().stream()
                .map(fc -> FlashcardSuggestion.builder()
                    .id(null)  // ID will be generated when saved to database
                    .sessionId(sessionId)
                    .frontContent(fc.getFront())
                    .backContent(fc.getBack())
                    .build())
                .toList();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse flashcard response: {}", content, e);
            throw new InvalidResponseException("Invalid response format", e);
        }
    }

    private int estimateTokenCount(String text) {
        int charCount = text.length();
        int estimatedInputTokens = charCount / 3;  // Polish language approximation

        int systemMessageTokens = 200;
        int estimatedOutputTokens = config.getDefaultParameters().getMaxTokens();

        return estimatedInputTokens + systemMessageTokens + estimatedOutputTokens;
    }

    private BigDecimal calculateCost(String model, int tokens) {
        Map<String, BigDecimal> costPer1kTokens = Map.of(
            "openai/gpt-4-turbo", new BigDecimal("0.01"),
            "openai/gpt-4o-mini", new BigDecimal("0.00015"),
            "openai/gpt-3.5-turbo", new BigDecimal("0.002"),
            "anthropic/claude-3-sonnet", new BigDecimal("0.003")
        );

        BigDecimal rate = costPer1kTokens.getOrDefault(
            model, new BigDecimal("0.01"));

        return rate.multiply(new BigDecimal(tokens))
            .divide(new BigDecimal(1000), 4, RoundingMode.HALF_UP);
    }
}