package com.ten.devs.cards.cards.user.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.generated.api.AuthenticationApi;
import com.ten.devs.cards.cards.generated.model.LoginRequest;
import com.ten.devs.cards.cards.generated.model.LoginResponse;
import com.ten.devs.cards.cards.generated.model.RegisterUserRequest;
import com.ten.devs.cards.cards.user.application.command.LoginUserCommand;
import com.ten.devs.cards.cards.user.application.command.RegisterUserCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

    private final Pipeline cqsService;

    @Override
    public ResponseEntity<UUID> register(RegisterUserRequest registerUserRequest) {
        log.info("Register request received: username={}, email={}, firstName={}, lastName={}",
                registerUserRequest.getUsername(),
                registerUserRequest.getEmail(),
                registerUserRequest.getFirstName(),
                registerUserRequest.getLastName());

        var registeredUserId = cqsService.send(
                RegisterUserCommand.builder()
                        .username(registerUserRequest.getUsername())
                        .password(registerUserRequest.getPassword())
                        .email(registerUserRequest.getEmail())
                        .firstName(registerUserRequest.getFirstName())
                        .lastName(registerUserRequest.getLastName())
                        .build());

        return ResponseEntity.ok(registeredUserId.userId());
    }

    @Override
    public ResponseEntity<LoginResponse> authenticate(LoginRequest loginRequest) {
        log.info("Login request received: username={}", loginRequest.getUsername());

        LoginUserCommand command = LoginUserCommand.builder()
                .username(loginRequest.getUsername())
                .password(loginRequest.getPassword())
                .build();

        com.ten.devs.cards.cards.user.presentation.response.LoginResponse domainResponse = cqsService.send(command);

        LoginResponse response = new LoginResponse();
        response.setUsername(domainResponse.username());
        response.setAccessToken(domainResponse.accessToken());
        response.setExpiresIn(domainResponse.expiresIn());

        return ResponseEntity.ok(response);
    }
}