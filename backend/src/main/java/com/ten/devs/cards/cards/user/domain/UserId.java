package com.ten.devs.cards.cards.user.domain;

import java.util.UUID;

public record UserId(UUID id) {
    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID id) {
        return new UserId(id);
    }
}
