package com.ten.devs.cards.cards.user.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.user.domain.Role;
import com.ten.devs.cards.cards.user.domain.User;
import com.ten.devs.cards.cards.user.domain.UserId;
import com.ten.devs.cards.cards.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class RegisterUserCommandHandler implements Command.Handler<RegisterUserCommand, UserId> {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserId handle(RegisterUserCommand command) {
        User user = User.newUser(
                UserId.random(),
                command.username(),
                passwordEncoder.encode(command.password()),
                command.email(),
                command.firstName(),
                command.lastName(),
                List.of(Role.USER)
        );

        userRepository.save(user);
        return user.toSnapshot().id();
    }
}
