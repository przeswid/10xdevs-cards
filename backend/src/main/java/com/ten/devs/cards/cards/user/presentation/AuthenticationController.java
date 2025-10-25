package com.ten.devs.cards.cards.user.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.user.presentation.request.LoginRequest;
import com.ten.devs.cards.cards.user.presentation.response.LoginResponse;
import com.ten.devs.cards.cards.user.application.command.LoginUserCommand;
import com.ten.devs.cards.cards.user.application.command.RegisterUserCommand;
import com.ten.devs.cards.cards.user.presentation.request.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final Pipeline cqsService;

    @PostMapping("/register")
    public ResponseEntity<UUID> register(@RequestBody RegisterUserRequest registerUserRequestDto) {
        var registeredUserId = cqsService.send(
                RegisterUserCommand.builder()
                        .username(registerUserRequestDto.username())
                        .password(registerUserRequestDto.password())
                        .email(registerUserRequestDto.email())
                        .firstName(registerUserRequestDto.firstName())
                        .lastName(registerUserRequestDto.lastName())
                        .build());

        return ResponseEntity.ok(registeredUserId.id());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest request) {
        LoginUserCommand command = LoginUserCommand.builder()
                .username(request.username())
                .password(request.password())
                .build();

        LoginResponse response = cqsService.send(command);
        return ResponseEntity.ok(response);
    }
}