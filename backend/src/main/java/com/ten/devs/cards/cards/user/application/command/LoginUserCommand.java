package com.ten.devs.cards.cards.user.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.user.presentation.response.LoginResponse;
import lombok.Builder;

@Builder
public record LoginUserCommand(String username, String password) implements Command<LoginResponse> {
}
