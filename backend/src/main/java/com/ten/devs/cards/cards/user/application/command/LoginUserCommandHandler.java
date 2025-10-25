package com.ten.devs.cards.cards.user.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.user.presentation.response.LoginResponse;
import com.ten.devs.cards.cards.user.domain.User;
import com.ten.devs.cards.cards.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class LoginUserCommandHandler implements Command.Handler<LoginUserCommand, LoginResponse> {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtOperations jwtOperations;

    @Override
    public LoginResponse handle(LoginUserCommand command) {
        // 1. Authenticate user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        command.username(),
                        command.password()
                )
        );

        // 2. Load user details for JWT generation
        UserDetails userDetails = userRepository.getUserByUsername(command.username())
                .map(User::toSnapshot)
                .map(u -> org.springframework.security.core.userdetails.User.builder()
                        .username(u.username())
                        .password(u.password())
                        .authorities(u.roles().stream().map(Enum::name).toList().toArray(new String[0]))
                        .build())
                .orElseThrow(() -> new RuntimeException("User not found: " + command.username()));

        // 3. Generate JWT token using JwtService
        String jwtToken = jwtOperations.generateToken(userDetails);

        // 4. Return login response with token and expiration
        return new LoginResponse(jwtToken, jwtOperations.getExpirationTime());
    }
}
