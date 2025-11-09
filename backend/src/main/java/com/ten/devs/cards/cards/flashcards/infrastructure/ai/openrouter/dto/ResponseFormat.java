package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseFormat {

    private String type;  // "json_schema"

    @JsonProperty("json_schema")
    private JsonSchema jsonSchema;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonSchema {
        private String name;
        private boolean strict;
        private Map<String, Object> schema;
    }
}