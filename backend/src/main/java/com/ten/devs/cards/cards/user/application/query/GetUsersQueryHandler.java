package com.ten.devs.cards.cards.user.application.query;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.user.domain.User;
import com.ten.devs.cards.cards.user.domain.UserRepository;
import com.ten.devs.cards.cards.user.domain.UserSnapshot;
import com.ten.devs.cards.cards.user.domain.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class GetUsersQueryHandler implements Command.Handler<GetUsersQuery, List<UserSnapshot>> {
    private final UserRepository userRepository;

    @Override
    public List<UserSnapshot> handle(GetUsersQuery query) {
        var specification = UserSpecification.builder()
                .usernameContains(query.username())
                .build();

        return userRepository.findAllBySpecification(specification).stream()
                .map(User::toSnapshot)
                .toList();
    }
}
