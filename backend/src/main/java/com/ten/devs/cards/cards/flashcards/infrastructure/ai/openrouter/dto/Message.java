package com.ten.devs.cards.cards.flashcards.infrastructure.ai.openrouter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String role;    // "system", "user", "assistant"
    private String content;

    public static Message system(String content) {
        return Message.builder()
            .role("system")
            .content(content)
            .build();
    }

    public static Message user(String content) {
        return Message.builder()
            .role("user")
            .content(content)
            .build();
    }

    public static Message assistant(String content) {
        return Message.builder()
            .role("assistant")
            .content(content)
            .build();
    }
}