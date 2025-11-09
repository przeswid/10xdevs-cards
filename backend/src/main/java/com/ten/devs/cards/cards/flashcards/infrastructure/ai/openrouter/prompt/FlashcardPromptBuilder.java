package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.prompt;

import com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.dto.ResponseFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FlashcardPromptBuilder {

    public String buildSystemMessage() {
        return """
            You are an expert in creating educational materials, specializing
            in generating study flashcards. Your task is to analyze the provided
            text and generate high-quality flashcards that will help students
            learn and remember key concepts.

            Requirements:
            - Generate 10-15 flashcards per request
            - Each flashcard must have a clear question (front) and concise answer (back)
            - Questions should test understanding, not just memorization
            - Answers must be accurate and concise (max 200 characters)
            - Focus on the most important concepts from the text
            - Avoid repetitions and trivial questions
            - Response must be in JSON format according to the provided schema
            """;
    }

    public String buildUserMessage(String inputText) {
        // Sanitization - basic protection against prompt injection
        String sanitized = inputText
            .replaceAll("```", "\\`\\`\\`")
            .replaceAll("(?i)ignore\\s+previous", "[FILTERED]")
            .replaceAll("(?i)system:", "[FILTERED]");

        return String.format("""
            Input text for flashcard generation:

            ---BEGIN INPUT---
            %s
            ---END INPUT---

            Generate flashcards covering the main concepts from the above text.
            Remember the quality requirements and return response in specified JSON format.
            """, sanitized);
    }

    public ResponseFormat buildResponseFormat() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "flashcards", Map.of(
                    "type", "array",
                    "description", "List of generated flashcards",
                    "items", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "front", Map.of(
                                "type", "string",
                                "description", "Question or prompt for flashcard",
                                "minLength", 5,
                                "maxLength", 500
                            ),
                            "back", Map.of(
                                "type", "string",
                                "description", "Answer or flashcard content",
                                "minLength", 5,
                                "maxLength", 200
                            )
                        ),
                        "required", List.of("front", "back"),
                        "additionalProperties", false
                    ),
                    "minItems", 5,
                    "maxItems", 20
                )
            ),
            "required", List.of("flashcards"),
            "additionalProperties", false
        );

        return ResponseFormat.builder()
            .type("json_schema")
            .jsonSchema(ResponseFormat.JsonSchema.builder()
                .name("flashcard_generation_response")
                .strict(true)
                .schema(schema)
                .build())
            .build();
    }
}