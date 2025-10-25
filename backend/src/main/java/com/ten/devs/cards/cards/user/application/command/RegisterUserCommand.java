package com.ten.devs.cards.cards.user.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.user.domain.UserId;
import lombok.Builder;

@Builder
public record RegisterUserCommand(String username, String password, String email, String firstName, String lastName) implements Command<UserId> {
}
