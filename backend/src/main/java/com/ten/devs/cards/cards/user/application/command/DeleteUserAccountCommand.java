package com.ten.devs.cards.cards.user.application.command;

import an.awesome.pipelinr.Command;
import lombok.Builder;

import java.util.UUID;

/**
 * Command for user account deletion (GDPR compliance)
 * Maps to DELETE /auth/account endpoint
 * Based on users table entity with CASCADE DELETE for related data
 */
@Builder
public record DeleteUserAccountCommand(
    UUID userId
) implements Command<Void> {
}