package com.ten.devs.cards.cards.user.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.user.presentation.response.RegisterResponse;
import lombok.Builder;

/**
 * Command for user registration
 * Maps to POST /auth/register endpoint
 * Based on users table entity fields
 */
@Builder
public record RegisterUserCommand(
    String username, 
    String password, 
    String email, 
    String firstName, 
    String lastName
) implements Command<RegisterResponse> {
}
