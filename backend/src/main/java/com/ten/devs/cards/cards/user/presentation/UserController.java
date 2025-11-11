package com.ten.devs.cards.cards.user.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.generated.api.UsersApi;
import com.ten.devs.cards.cards.generated.model.GetUserResponse;
import com.ten.devs.cards.cards.user.application.query.GetUsersQuery;
import com.ten.devs.cards.cards.user.domain.UserSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
class UserController implements UsersApi {

    private final Pipeline cqsService;

    @Override
    public ResponseEntity<List<GetUserResponse>> allUsers() {
        log.info("Get all users request received");

        GetUsersQuery query = new GetUsersQuery(null);
        List<UserSnapshot> userSnapshots = cqsService.send(query);

        List<GetUserResponse> responses = userSnapshots.stream()
                .map(snapshot -> new GetUserResponse(
                        snapshot.id().id(),  // Convert UserId to UUID
                        snapshot.username(),
                        snapshot.email(),
                        snapshot.firstName(),
                        snapshot.lastName()
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }
}